package com.sd.lib.dlcache

import java.io.File

interface IDiskLruCache {

    fun setMaxSize(maxSize: Long)

    fun put(key: String, file: File?): Boolean

    fun get(key: String): File?

    fun remove(key: String): Boolean

    fun size(): Long

    fun edit(key: String, block: (editFile: File) -> Boolean): Boolean

    fun close()
}