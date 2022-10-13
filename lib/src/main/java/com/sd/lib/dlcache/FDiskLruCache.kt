package com.sd.lib.dlcache

import com.sd.lib.dlcache.core.DiskLruCache
import java.io.File
import java.io.IOException
import java.security.MessageDigest

class FDiskLruCache private constructor(
    directory: File,
    maxSize: Long,
) {
    init {
        require(maxSize > 0) { "require maxSize > 0" }
    }

    private val _directory = directory
    private var _maxSize = maxSize

    private var _diskLruCache: DiskLruCache? = null

    var keyTransform: KeyTransform = MD5KeyTransform()

    /**
     * 设置最大容量
     */
    @Synchronized
    fun setMaxSize(maxSize: Long) {
        require(maxSize > 0) { "require maxSize > 0" }
        if (_maxSize != maxSize) {
            _maxSize = maxSize
            _diskLruCache?.maxSize = maxSize
        }
    }

    @Synchronized
    fun put(key: String, file: File?): Boolean {
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
    fun get(key: String): File? {
        if (key.isEmpty()) return null
        val cache = openCache() ?: return null

        val key = keyTransform.transform(key)
        check(key.isNotEmpty()) { "transform key is empty" }

        return try {
            val file = cache.get(key)?.getFile(0)
            if (file?.exists() == true) file else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        val key = keyTransform.transform(key)
        check(key.isNotEmpty()) { "transform key is empty" }

        return try {
            cache.remove(key)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    @Synchronized
    fun size(): Long {
        val cache = openCache() ?: return 0L
        return cache.size()
    }

    /**
     * 编辑文件
     */
    @Synchronized
    fun edit(key: String, block: (editFile: File) -> Boolean): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        val key = keyTransform.transform(key)
        check(key.isNotEmpty()) { "transform key is empty" }

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

    fun interface KeyTransform {
        fun transform(key: String): String
    }

    companion object {
        private val _cacheMap: MutableMap<String, FDiskLruCache> = mutableMapOf()

        @JvmOverloads
        @JvmStatic
        fun dir(directory: File, maxSize: Long = 500 * 1024 * 1024): FDiskLruCache {
            return synchronized(this@Companion) {
                val path = directory.absolutePath
                _cacheMap[path] ?: FDiskLruCache(directory, maxSize).also {
                    _cacheMap[path] = it
                }
            }.also {
                it.setMaxSize(maxSize)
            }
        }
    }
}

class MD5KeyTransform : FDiskLruCache.KeyTransform {
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