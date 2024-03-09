package com.sd.lib.dlcache

import android.util.Base64
import com.sd.lib.closeable.FAutoCloseFactory
import java.io.File
import java.io.IOException

interface FDiskLruCache {
    /**
     * 设置缓存最大值，单位byte
     */
    fun setMaxSize(maxSize: Long)

    /**
     * 保存文件到缓存
     */
    fun put(key: String, file: File?): Boolean

    /**
     * 获取[key]对应的缓存文件，返回的文件只能读取，不能修改和删除
     */
    fun get(key: String): File?

    /**
     * 移除[key]对应的缓存
     */
    fun remove(key: String): Boolean

    /**
     * 当前缓存大小
     */
    fun size(): Long

    /**
     * 编辑[key]对应的缓存文件，[block]返回true表示编辑成功
     */
    fun edit(key: String, block: (editFile: File) -> Boolean): Boolean

    companion object {
        private val sFactory = FAutoCloseFactory(CloseableDiskLruCache::class.java)

        @JvmStatic
        fun get(directory: File): FDiskLruCache {
            val path = directory.absolutePath
            return sFactory.create(path) { DiskLruCacheImpl(directory) }
        }
    }
}

private interface CloseableDiskLruCache : FDiskLruCache, AutoCloseable

private class DiskLruCacheImpl(
    private val directory: File
) : CloseableDiskLruCache {

    private var _cache: DiskLruCache? = null
    private var _maxSize = 200 * 1024 * 1024L

    init {
        if (directory.isFile) error("directory is file.")
    }

    @Synchronized
    override fun setMaxSize(maxSize: Long) {
        require(maxSize > 0) { "require maxSize > 0" }
        if (_maxSize != maxSize) {
            _maxSize = maxSize
            _cache?.maxSize = maxSize
        }
    }

    override fun put(key: String, file: File?): Boolean {
        if (file == null) return false
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

        @Suppress("NAME_SHADOWING")
        val key = transformKey(key)

        return try {
            val file = cache.get(key)?.getFile(0)
            if (file?.isFile == true) file else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    override fun remove(key: String): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        @Suppress("NAME_SHADOWING")
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
        return openCache()?.size() ?: 0L
    }

    @Synchronized
    override fun edit(key: String, block: (editFile: File) -> Boolean): Boolean {
        if (key.isEmpty()) return false
        val cache = openCache() ?: return false

        @Suppress("NAME_SHADOWING")
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
            editor.abortQuietly()
            false
        }
    }

    @Synchronized
    private fun openCache(): DiskLruCache? {
        _cache?.let { return it }
        return try {
            DiskLruCache.open(directory, 1, 1, _maxSize).also { _cache = it }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    override fun close() {
        try {
            _cache?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            _cache = null
        }
    }

    private fun transformKey(key: String): String {
        require(key.isNotEmpty())
        return key.encodeKey().also {
            check(it.isNotEmpty()) { "transform key is empty" }
        }
    }
}

private fun String.encodeKey(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP
    return Base64.encode(input, flag).decodeToString()
}

private fun DiskLruCache.Editor.abortQuietly() {
    try {
        abort()
    } catch (ignored: IOException) {
    }
}