package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentRingingBinding
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
import com.bumptech.glide.Glide
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RingingFragment : Fragment() {

    private var _binding:
            FragmentRingingBinding? = null

    private val binding
        get() = _binding!!

    private var mediaPlayer:
            MediaPlayer? = null

    private var ringingJob:
            Job? = null

    private var heroName = ""
    private var videoUrl = ""
    private var audioUrl = ""

    private var delayTime = 0L
    private var isVideoCall = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentRingingBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        disableBackPress()

        heroName =
            arguments
                ?.getString("name")
                .orEmpty()


        videoUrl =
            arguments
                ?.getString("videoUrl")
                .orEmpty()


        audioUrl =
            arguments
                ?.getString("audioUrl")
                .orEmpty()


        delayTime =
            arguments
                ?.getLong("delay")
                ?: 0L


        isVideoCall =
            arguments
                ?.getBoolean("isVideoCall")
                ?: false


        binding.tvName.text =
            heroName

        Glide.with(requireContext())
            .load(videoUrl)
            .centerCrop()
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .thumbnail(0.1f)
            .into(binding.ivCaller)
        if (isVideoCall) {

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

                startCamera()
            }

        } else {

            binding.previewView.visibility =
                View.GONE
        }


        binding.btnEndCallConnecting
            .setOnClickListener {

                ringingJob?.cancel()

                stopRinging()

                findNavController()
                    .popBackStack()
            }


        startRinging()


        ringingJob =
            viewLifecycleOwner
                .lifecycleScope
                .launch {

                    delay(4000)


                    if (
                        !isAdded ||
                        _binding == null
                    ) {
                        return@launch
                    }


                    stopRinging()


                    findNavController().navigate(

                        R.id.action_ringingFragment_to_videoCallFragment,

                        bundleOf(

                            "delay" to delayTime,

                            "name" to heroName,

                            /*
                             * This is now the LOCAL
                             * cached video path.
                             */
                            "videoUrl" to videoUrl,

                            "audioUrl" to audioUrl
                        )
                    )
                }
    }


    private fun startRinging() {

        mediaPlayer?.release()


        mediaPlayer =
            MediaPlayer.create(
                requireContext(),
                R.raw.ringing
            ).apply {

                isLooping = true

                start()
            }
    }


    private fun stopRinging() {

        mediaPlayer?.let {

            if (it.isPlaying) {
                it.stop()
            }

            it.release()
        }


        mediaPlayer = null
    }


    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(
                    requireContext()
                )


        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()


            val preview =
                Preview.Builder()
                    .build()
                    .also {

                        it.surfaceProvider =
                            binding.previewView
                                .surfaceProvider
                    }


            val cameraSelector =
                CameraSelector
                    .DEFAULT_FRONT_CAMERA


            cameraProvider.unbindAll()


            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview
            )

        }, ContextCompat.getMainExecutor(
            requireContext()
        ))
    }


    override fun onDestroyView() {

        ringingJob?.cancel()

        stopRinging()

        _binding = null

        super.onDestroyView()
    }
}
