package com.sd.demo.disk_lru_cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.disk_lru_cache.databinding.ActivityMainBinding
import com.sd.lib.dlcache.FDiskLruCache
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val _diskLruCache by lazy {
        val directory = externalCacheDir!!
        FDiskLruCache(directory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnPut.setOnClickListener {
            putCache()
        }
        _binding.btnGet.setOnClickListener {
            getCache()
        }
        _binding.btnRemove.setOnClickListener {
            removeCache()
        }
    }

    private fun putCache() {
        val fileContent = UUID.randomUUID().toString()
        val tempFile = File.createTempFile("lru", ".tmp").apply {
            writeText(fileContent)
        }

        val put = _diskLruCache.put("key", tempFile)
        tempFile.delete()

        logMsg { "putCache $fileContent $put" }
    }

    private fun getCache() {
        val readContent = _diskLruCache.get("key")?.readText()
        logMsg { "getCache $readContent" }
    }

    private fun removeCache() {
        val remove = _diskLruCache.remove("key")
        logMsg { "removeCache $remove" }
    }

    override fun onStop() {
        super.onStop()
        _diskLruCache.close()
    }
}

fun logMsg(block: () -> String) {
    Log.i("disk-lru-cache-demo", block())
}