package com.sd.lib.dlcache

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class FDiskLruCache(directory: File) : IDiskLruCache {
    private val _directory = directory
    private val cache: IDiskLruCache
        get() = InternalDiskLruCache.open(_directory)

    val cacheId: String
        get() = cache.toString()

    init {
        require(directory.isDirectory)
        addCount(directory)
    }

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

    protected fun finalize() {
        removeCount(_directory)
    }

    companion object {
        private val _counterHolder: MutableMap<String, AtomicInteger> = hashMapOf()

        private fun addCount(directory: File) {
            synchronized(this@Companion) {
                val path = directory.absolutePath
                val counter = _counterHolder[path] ?: AtomicInteger(0).also {
                    _counterHolder[path] = it
                }
                counter.incrementAndGet()
            }
        }

        private fun removeCount(directory: File) {
            synchronized(this@Companion) {
                val path = directory.absolutePath
                val counter = _counterHolder[path] ?: error("Directory was not found $path")
                counter.decrementAndGet().also {
                    if (it <= 0) {
                        _counterHolder.remove(path)
                        InternalDiskLruCache.close(directory)
                    }
                }
            }
        }
    }
}