package com.sd.lib.dlcache

import java.io.File

class FDiskLruCache(directory: File) : IDiskLruCache {

    private val _directory = directory
    private val cache: IDiskLruCache
        get() = InternalDiskLruCache.dir(_directory)

    override fun setMaxSize(maxSize: Long) {
        cache.setMaxSize(maxSize)
    }

    override fun put(key: String, file: File?): Boolean {
        return cache.put(key, file)
    }

    override fun get(key: String): File? {
        return cache.get(key)
    }

    override fun remove(key: String): Boolean {
        return cache.remove(key)
    }

    override fun size(): Long {
        return cache.size()
    }

    override fun edit(key: String, block: (editFile: File) -> Boolean): Boolean {
        return cache.edit(key, block)
    }

    override fun close() {
        cache.close()
    }
}