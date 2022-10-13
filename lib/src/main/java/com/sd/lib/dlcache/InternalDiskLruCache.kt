package com.sd.lib.dlcache

import com.sd.lib.dlcache.core.DiskLruCache
import java.io.File
import java.io.IOException
import java.security.MessageDigest

internal class InternalDiskLruCache private constructor(directory: File) : IDiskLruCache {

    private val _directory = directory
    private var _maxSize = 500 * 1024 * 1024L

    private var _diskLruCache: DiskLruCache? = null
    private var _keyTransform = MD5KeyTransform()

    /**
     * 设置最大容量
     */
    @Synchronized
    override fun setMaxSize(maxSize: Long) {
        require(maxSize > 0) { "require maxSize > 0" }
        if (_maxSize != maxSize) {
            _maxSize = maxSize
            _diskLruCache?.maxSize = maxSize
        }
    }

    @Synchronized
    override fun put(key: String, file: File?): Boolean {
        if (file == null) return false
        if (!file.exists()) return false
        if (!file.isFile) return false
        return edit(key) { editFile ->
            try {
                file.copyTo(editFile, overwrite = true)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @Synchronized
    override fun get(key: String): File? {
        if (key.isEmpty()) return null
        val cache = openCache() ?: return null

        val key = transformKey(key)

        return try {
            val file = cache.get(key)?.getFile(0)
            if (file?.exists() == true) file else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    override fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        val key = transformKey(key)

        return try {
            cache.remove(key)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    @Synchronized
    override fun size(): Long {
        val cache = openCache() ?: return 0L
        return cache.size()
    }

    /**
     * 编辑文件
     */
    @Synchronized
    override fun edit(key: String, block: (editFile: File) -> Boolean): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        val key = transformKey(key)

        val editor = try {
            cache.edit(key)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } ?: return false

        val file = try {
            editor.getFile(0)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        if (file == null) {
            editor.abortQuietly()
            return false
        }

        if (!block(file)) {
            editor.abortQuietly()
            return false
        }

        return try {
            editor.commit()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            editor.abortUnlessCommitted()
        }
    }

    override fun close() {
        closeDir(_directory)
    }

    @Synchronized
    private fun openCache(): DiskLruCache? {
        val cache = _diskLruCache
        if (cache != null) return cache

        return try {
            DiskLruCache.open(_directory, 1, 1, _maxSize).also {
                _diskLruCache = it
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    private fun closeCache() {
        _diskLruCache?.close()
        /**
         * 不要把_diskLruCache设置为null，否则[openCache]重新打开的话，有可能造成多个对象管理同一个目录
         */
    }

    private fun transformKey(key: String): String {
        return _keyTransform.transform(key).also {
            check(it.isNotEmpty()) { "transform key is empty" }
        }
    }

    internal fun interface KeyTransform {
        fun transform(key: String): String
    }

    companion object {
        private val _cacheMap: MutableMap<String, InternalDiskLruCache> = mutableMapOf()

        fun dir(directory: File): InternalDiskLruCache {
            return synchronized(this@Companion) {
                val path = directory.absolutePath
                _cacheMap[path] ?: InternalDiskLruCache(directory).also {
                    _cacheMap[path] = it
                }
            }
        }

        private fun closeDir(directory: File) {
            synchronized(this@Companion) {
                val path = directory.absolutePath
                _cacheMap.remove(path)
            }?.closeCache()
        }
    }
}

private class MD5KeyTransform : InternalDiskLruCache.KeyTransform {
    override fun transform(key: String): String {
        return md5(key)
    }

    private fun md5(content: String): String {
        return try {
            val bytes = MessageDigest.getInstance("MD5").digest(content.toByteArray())
            bytes.joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            content
        }
    }
}

private fun DiskLruCache.Editor.abortQuietly() {
    try {
        abort()
    } catch (ignored: IOException) {
    }
}