package com.sd.demo.disk_lru_cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sd.lib.dlcache.FDiskLruCache
import com.sd.lib.dlcache.IDiskLruCache
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
        val cachePut = getCache()
        val cacheGet = getCache()
        val cacheRemove = getCache()

        // test
        testPutGetRemove(
            cachePut = cachePut,
            cacheGet = cacheGet,
            cacheRemove = cacheRemove,
        )

        // cache id
        val cacheId = cachePut.cacheId
        assertEquals(cacheId, cachePut.cacheId)
        assertEquals(cacheId, cacheGet.cacheId)
        assertEquals(cacheId, cacheRemove.cacheId)
    }

    private fun testPutGetRemove(
        cachePut: IDiskLruCache,
        cacheGet: IDiskLruCache,
        cacheRemove: IDiskLruCache,
    ) {
        val fileContent = UUID.randomUUID().toString()
        val tempFile = File.createTempFile("lru", ".tmp").apply {
            writeText(fileContent)
        }

        // put
        assertEquals(true, cachePut.put("key", tempFile))
        tempFile.delete()

        // get
        val readFile = cacheGet.get("key")
        val readContent = readFile?.readText()
        assertEquals(fileContent, readContent)

        // remove
        assertEquals(true, cacheRemove.remove("key"))
        assertEquals(null, cacheRemove.get("key"))
    }

    private fun getCache(): FDiskLruCache {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.externalCacheDir!!
        return FDiskLruCache(directory)
    }
}