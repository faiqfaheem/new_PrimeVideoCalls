package com.axis.vpn.tools.prankvideocall.data.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class SoundCacheManager(
    private val context: Context
) {

    private val soundDir = File(context.filesDir, "prank_sounds")

    init {
        if (!soundDir.exists()) {
            soundDir.mkdirs()
        }
    }

    fun getSoundFile(soundUrl: String): File {
        val fileName = soundUrl.substringAfterLast("/")
        return File(soundDir, fileName)
    }

    fun isDownloaded(soundUrl: String): Boolean {
        return getSoundFile(soundUrl).exists()
    }

    suspend fun downloadSound(soundUrl: String): File =
        withContext(Dispatchers.IO) {

            val file = getSoundFile(soundUrl)

            if (file.exists()) {
                return@withContext file
            }

            URL(soundUrl).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            file
        }
}