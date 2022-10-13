package com.sd.demo.disk_lru_cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.disk_lru_cache.databinding.ActivityMainBinding
import com.sd.lib.dlcache.FDiskLruCache
import com.sd.lib.io.FFileUtils
import com.sd.lib.io.FIOUtils
import com.sd.lib.io.dir.ext.FDirTemp
import java.util.*

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val _diskLruCache by lazy {
        FDiskLruCache.dir(FFileUtils.getCacheDir("app_cache"))
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
        val tempFile = FDirTemp.newFile("txt")
        FIOUtils.writeText(UUID.randomUUID().toString(), tempFile)

        _diskLruCache.put("key", tempFile)
    }

    private fun getCache() {
        val file = _diskLruCache.get("key") ?: return
        Log.i(TAG, FIOUtils.readText(file) ?: "")
    }

    private fun removeCache() {
        _diskLruCache.remove("key")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}