package com.sd.demo.disk_lru_cache

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.sd.demo.disk_lru_cache.databinding.ActivityMainBinding
import com.sd.lib.dlcache.FDiskLruCache
import com.sd.lib.dlcache.IDiskLruCache
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var _cache: IDiskLruCache? = null

    private val cache: IDiskLruCache
        get() = _cache ?: FDiskLruCache.get(externalCacheDir!!).also { _cache = it }

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

        val put = cache.put("key", tempFile)
        tempFile.delete()

        logMsg { "putCache $fileContent $put" }
    }

    private fun getCache() {
        val readContent = cache.get("key")?.readText()
        logMsg { "getCache $readContent" }
    }

    private fun removeCache() {
        val remove = cache.remove("key")
        logMsg { "removeCache $remove" }
    }

    override fun onStop() {
        super.onStop()
        _cache = null
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("disk-lru-cache-demo", block())
}