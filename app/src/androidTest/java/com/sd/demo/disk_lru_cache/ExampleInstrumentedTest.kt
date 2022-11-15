package com.sd.demo.disk_lru_cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sd.lib.dlcache.FDiskLruCache
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun testCache() {
        val fileContent = UUID.randomUUID().toString()
        val tempFile = File.createTempFile("lru", ".tmp").apply {
            writeText(fileContent)
        }

        // put
        val cachePut = getCache()
        assertEquals(true, cachePut.put("key", tempFile))
        tempFile.delete()

        // get
        val cacheGet = getCache()
        val readFile = cacheGet.get("key")
        val readContent = readFile?.readText()
        assertEquals(fileContent, readContent)

        // remove
        val cacheRemove = getCache()
        assertEquals(true, cacheRemove.remove("key"))
        assertEquals(null, cacheRemove.get("key"))

        // cache id
        assert(cachePut.cacheId == cacheGet.cacheId)
        assert(cachePut.cacheId == cacheRemove.cacheId)
    }

    private fun getCache(): FDiskLruCache {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.externalCacheDir!!
        return FDiskLruCache(directory)
    }
}