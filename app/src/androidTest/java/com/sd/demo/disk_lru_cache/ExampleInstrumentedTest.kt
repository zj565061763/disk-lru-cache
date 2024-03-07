package com.sd.demo.disk_lru_cache

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sd.lib.dlcache.FDiskLruCache
import com.sd.lib.dlcache.IDiskLruCache
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

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

        val key = "key key *./"

        // put
        assertEquals(true, cachePut.put(key, tempFile))
        tempFile.delete()

        // get
        assertEquals(fileContent, cachePut.get(key)?.readText())
        assertEquals(fileContent, cacheGet.get(key)?.readText())
        assertEquals(fileContent, cacheRemove.get(key)?.readText())

        // remove
        assertEquals(true, cacheRemove.remove(key))
        assertEquals(null, cachePut.get(key))
        assertEquals(null, cacheGet.get(key))
        assertEquals(null, cacheRemove.get(key))
    }

    private fun getCache(): IDiskLruCache {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.externalCacheDir!!
        return FDiskLruCache.get(directory)
    }
}