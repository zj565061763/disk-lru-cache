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
        val fileContent = UUID.randomUUID().toString()
        val tempFile = File.createTempFile("lru", ".tmp").apply {
            writeText(fileContent)
        }

        assertEquals(true, getCache().put("key", tempFile))
        tempFile.delete()

        val readFile = getCache().get("key")
        val readContent = readFile?.readText()

        assertEquals(fileContent, readContent)
    }

    private fun getCache(): IDiskLruCache {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.externalCacheDir!!
        return FDiskLruCache(directory)
    }
}