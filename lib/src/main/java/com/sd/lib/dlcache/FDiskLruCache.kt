package com.sd.lib.dlcache

import java.io.File

class FDiskLruCache(directory: File) : IDiskLruCache {

    private val _cache by lazy { InternalDiskLruCache.dir(directory) }

    override fun setMaxSize(maxSize: Long) {
        _cache.setMaxSize(maxSize)
    }

    override fun put(key: String, file: File?): Boolean {
        return _cache.put(key, file)
    }

    override fun get(key: String): File? {
        return _cache.get(key)
    }

    override fun remove(key: String): Boolean {
        return _cache.remove(key)
    }

    override fun size(): Long {
        return _cache.size()
    }

    override fun edit(key: String, block: (editFile: File) -> Boolean): Boolean {
        return _cache.edit(key, block)
    }

    override fun close() {
        _cache.close()
    }
}