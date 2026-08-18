package com.axis.vpn.tools.prankvideocall.utils.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.File

fun Fragment.showPermissionSettingsDialog(
    title: String = "Permission Required",
    message: String = "This permission is required to use this feature. Please enable it from Settings.",
    positiveButton: String = "Settings",
    negativeButton: String = "Cancel"
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(positiveButton) { _, _ ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().packageName, null)
            )
            startActivity(intent)
        }
        .setNegativeButton(negativeButton, null)
        .show()
}
fun Uri.getFileExtension(context: Context): String {
    return try {
        when (context.contentResolver.getType(this)) {
            null -> "tmp"
            else -> {
                val mimeType = context.contentResolver.getType(this)
                when {
                    mimeType?.startsWith("image/") == true -> "jpg"
                    mimeType?.startsWith("video/") == true -> "mp4"
                    mimeType?.startsWith("audio/") == true -> "m4a"
                    else -> "tmp"
                }
            }
        }
    } catch (_: Exception) {
        "tmp"
    }
}
fun Context.getFileName(uri: Uri): String {
    var fileName = ""

    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index != -1) {
            fileName = cursor.getString(index)
        }
    }

    return fileName
}
fun Context.copyFileToAppStorage(
    uri: Uri,
    fileType: String,
    folderName: String = "callers"
): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: run {
            Log.e("FILE_COPY", "Could not open input stream")
            return null
        }

        val appStorageDir = File(filesDir, folderName).apply {
            if (!exists()) mkdirs()
        }

        val fileName =
            "${fileType}_${System.currentTimeMillis()}.${uri.getFileExtension(this)}"

        val outputFile = File(appStorageDir, fileName)

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Log.d("FILE_COPY", "File copied to: ${outputFile.absolutePath}")
        outputFile.absolutePath
    } catch (e: Exception) {
        Log.e("FILE_COPY", "Error copying file", e)
        null
    }
}
fun Activity.hideKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }
}
fun Fragment.setEdgeToEdgePadding(
    view: View,
    enableKeyboardInsets: Boolean = false,
    isLightStatusBars: Boolean = true
) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->

        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = isLightStatusBars

        if (enableKeyboardInsets) {
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                imeInsets.bottom
            )
        } else {
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
        }

        WindowInsetsCompat.CONSUMED
    }
}
fun Fragment.disableBackPress() {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, disables back press
            }
        }
    )
}


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun Fragment.navigateTo(fragmentId: Int, action: Int, bundle: Bundle? = null) {
    launchWhenCreated {
        if (isAdded && isCurrentDestination(fragmentId)) {
            findNavController().navigate(action ,  bundle)
        }
    }
}


private fun Fragment.isCurrentDestination(fragmentId: Int): Boolean {
    return findNavController().currentDestination?.id == fragmentId
}