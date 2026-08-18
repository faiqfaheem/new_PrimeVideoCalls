package com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.data.viewModels.CallerViewModel
import com.axis.vpn.tools.prankvideocall.databinding.BottomSheetCustomCallerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.getValue
class CustomCallerBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: CallerViewModel by viewModel()

    private var _binding: BottomSheetCustomCallerBinding? = null
    private val binding get() = _binding!!

    private var imagePath: String? = null
    private var videoPath: String? = null
    private var audioPath: String? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    val path = withContext(Dispatchers.IO) {
                        copyFileToAppStorage(it, "image")
                    }
                    imagePath = path
                    if (path != null) {
                        binding.ivProfile.setImageURI(Uri.parse(path))
                        Log.d("IMAGE_PICKER", "Image saved to: $path")
                    } else {
                        Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    videoPath = copyFileToAppStorage(it, "video")
                    if (videoPath != null) {
                        binding.tvVideo.text = getFileName(it)
                        Log.d("VIDEO_PICKER", "Video saved to: $videoPath")
                    }
                } catch (e: Exception) {
                    Log.e("VIDEO_PICKER", "Failed to copy video", e)
                    Toast.makeText(requireContext(), "Failed to save video", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val audioPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    audioPath = copyFileToAppStorage(it, "audio")
                    if (audioPath != null) {
                        binding.tvAudio.text = getFileName(it)
                        Log.d("AUDIO_PICKER", "Audio saved to: $audioPath")
                    }
                } catch (e: Exception) {
                    Log.e("AUDIO_PICKER", "Failed to copy audio", e)
                    Toast.makeText(requireContext(), "Failed to save audio", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomCallerBinding.inflate(inflater, container, false)
        setupClicks()
        return binding.root
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        requireContext().contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index != -1) {
                name = cursor.getString(index)
            }
        }
        return name
    }

    /**
     * Copy file from picker URI to app's private storage
     */
    private fun copyFileToAppStorage(uri: Uri, fileType: String): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("FILE_COPY", "Could not open input stream")
                return null
            }

            val appStorageDir = File(requireContext().filesDir, "callers")
            if (!appStorageDir.exists()) {
                appStorageDir.mkdirs()
            }

            val fileName = "${fileType}_${System.currentTimeMillis()}.${getFileExtension(uri)}"
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

    /**
     * Get file extension from URI
     */
    private fun getFileExtension(uri: Uri): String {
        return try {
            val mimeType = requireContext().contentResolver.getType(uri)
            when {
                mimeType?.contains("image") == true -> "jpg"
                mimeType?.contains("video") == true -> "mp4"
                mimeType?.contains("audio") == true -> "m4a"
                else -> "tmp"
            }
        } catch (e: Exception) {
            "tmp"
        }
    }

    private fun setupClicks() {
        binding.btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnSelectVideo.setOnClickListener {
            videoPicker.launch("video/*")
        }

        binding.btnSelectAudio.setOnClickListener {
            audioPicker.launch("audio/*")
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = "Enter Name"
                return@setOnClickListener
            }

            if (imagePath == null) {
                Toast.makeText(requireContext(), "Select Image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (videoPath == null) {
                Toast.makeText(requireContext(), "Select Video", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            saveCaller(name)
        }
    }

    private fun saveCaller(name: String) {
        val caller = CustomCallerEntity(
            name = name,
            imagePath = imagePath ?: "",
            videoUri = videoPath ?: "",
            audioUri = audioPath ?: ""
        )

        viewModel.insertCaller(caller)

        Toast.makeText(requireContext(), "Caller Saved", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}