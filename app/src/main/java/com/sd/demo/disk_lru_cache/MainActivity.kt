package com.sd.demo.disk_lru_cache

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.demo.disk_lru_cache.ui.theme.AppTheme
import com.sd.lib.dlcache.FDiskLruCache
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val _cache by lazy { FDiskLruCache(externalCacheDir!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content(
                    onClickPut = {
                        putCache()
                    },
                    onClickGet = {
                        getCache()
                    },
                    onClickRemove = {
                        removeCache()
                    },
                )
            }
        }
    }

    private fun putCache() {
        val fileContent = UUID.randomUUID().toString()
        val tempFile = File.createTempFile("lru", ".tmp").apply {
            writeText(fileContent)
        }

        val put = _cache.put("key", tempFile)
        tempFile.delete()

        logMsg { "putCache $fileContent $put" }
    }

    private fun getCache() {
        val readContent = _cache.get("key")?.readText()
        logMsg { "getCache $readContent" }
    }

    private fun removeCache() {
        val remove = _cache.remove("key")
        logMsg { "removeCache $remove" }
    }
}

@Composable
private fun Content(
    onClickPut: () -> Unit,
    onClickGet: () -> Unit,
    onClickRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = onClickPut
        ) {
            Text(text = "put")
        }

        Button(
            onClick = onClickGet
        ) {
            Text(text = "get")
        }

        Button(
            onClick = onClickRemove
        ) {
            Text(text = "remove")
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("disk-lru-cache-demo", block())
}