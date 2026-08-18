package com.axis.vpn.tools.prankvideocall.utils.constants


import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object VideoCacheManager {

    private const val TAG = "VideoCacheManager"
    private const val CACHE_FOLDER = "prank_videos"

    private fun getCacheDirectory(
        context: Context
    ): File {

        return File(
            context.cacheDir,
            CACHE_FOLDER
        ).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun getCacheFile(
        context: Context,
        videoUrl: String
    ): File {

        val fileName =
            videoUrl.hashCode().toString() + ".mp4"

        return File(
            getCacheDirectory(context),
            fileName
        )
    }

    /**
     * Returns cached video if it exists.
     */
    fun getCachedVideo(
        context: Context,
        videoUrl: String
    ): File? {

        if (videoUrl.isBlank()) {
            return null
        }

        /*
         * If already a local file path.
         */
        val directFile = File(videoUrl)

        if (
            directFile.exists() &&
            directFile.isFile &&
            directFile.length() > 0
        ) {
            return directFile
        }

        val cacheFile =
            getCacheFile(
                context,
                videoUrl
            )

        return if (
            cacheFile.exists() &&
            cacheFile.isFile &&
            cacheFile.length() > 0
        ) {
            cacheFile
        } else {
            null
        }
    }

    /**
     * Returns cached file or downloads it.
     */
    suspend fun ensureVideoCached(
        context: Context,
        videoUrl: String
    ): File? = withContext(Dispatchers.IO) {

        if (videoUrl.isBlank()) {
            return@withContext null
        }

        /*
         * Already local file.
         */
        val directFile =
            File(videoUrl)

        if (
            directFile.exists() &&
            directFile.isFile &&
            directFile.length() > 0
        ) {
            return@withContext directFile
        }

        /*
         * Check cache.
         */
        val cachedFile =
            getCachedVideo(
                context,
                videoUrl
            )

        if (cachedFile != null) {
            Log.d(
                TAG,
                "Using cached video: ${cachedFile.absolutePath}"
            )

            return@withContext cachedFile
        }

        val outputFile =
            getCacheFile(
                context,
                videoUrl
            )

        val tempFile =
            File(
                outputFile.parentFile,
                outputFile.name + ".tmp"
            )

        try {

            Log.d(
                TAG,
                "Downloading: $videoUrl"
            )

            val connection =
                URL(videoUrl)
                    .openConnection()
                        as HttpURLConnection

            connection.connectTimeout =
                15_000

            connection.readTimeout =
                30_000

            connection.requestMethod =
                "GET"

            connection.connect()

            if (
                connection.responseCode !in
                200..299
            ) {

                Log.e(
                    TAG,
                    "HTTP error: ${connection.responseCode}"
                )

                connection.disconnect()

                return@withContext null
            }

            connection.inputStream.use { input ->

                FileOutputStream(
                    tempFile
                ).use { output ->

                    val buffer =
                        ByteArray(8 * 1024)

                    var bytesRead: Int

                    while (
                        input.read(buffer)
                            .also {
                                bytesRead = it
                            } != -1
                    ) {

                        output.write(
                            buffer,
                            0,
                            bytesRead
                        )
                    }

                    output.flush()
                }
            }

            connection.disconnect()

            /*
             * Rename only after successful download.
             */
            if (outputFile.exists()) {
                outputFile.delete()
            }

            if (!tempFile.renameTo(outputFile)) {

                tempFile.copyTo(
                    outputFile,
                    overwrite = true
                )

                tempFile.delete()
            }

            if (
                outputFile.exists() &&
                outputFile.length() > 0
            ) {

                Log.d(
                    TAG,
                    "Download complete: ${outputFile.absolutePath}"
                )

                return@withContext outputFile
            }

            null

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Video download failed",
                e
            )

            if (tempFile.exists()) {
                tempFile.delete()
            }

            null
        }
    }

    /**
     * Optional cleanup.
     */
    fun clearCache(
        context: Context
    ) {

        try {

            getCacheDirectory(context)
                .deleteRecursively()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Unable to clear cache",
                e
            )
        }
    }
}

