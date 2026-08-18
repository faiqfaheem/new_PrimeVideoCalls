package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.annotation.IdRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentFakeIncomingCallBinding
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
import com.axis.vpn.tools.prankvideocall.utils.extension.setEdgeToEdgePadding
import com.bumptech.glide.Glide
import java.io.File
class FakeIncomingCallFragment :
    Fragment(R.layout.fragment_fake_incoming_call) {

    private var _binding:
            FragmentFakeIncomingCallBinding? = null

    private val binding
        get() = _binding!!

    private var delay = 0L

    private var heroName = ""

    private var videoUrl = ""

    private var audioUrl = ""

    private var isCustom = false

    private var imagePath = ""

    private var dY = 0f

    private var ringtone:
            Ringtone? = null

    private var vibrator:
            Vibrator? = null

    private var cameraProvider:
            ProcessCameraProvider? = null

    private var isCameraOn = true

    private val TAG =
        "FakeIncomingCallFragment"

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val args =
            arguments

        delay =
            args?.getLong(
                "delay"
            ) ?: 0L

        heroName =
            args?.getString(
                "name"
            ).orEmpty()

        videoUrl =
            args?.getString(
                "videoUrl"
            ).orEmpty()

        audioUrl =
            args?.getString(
                "audioUrl"
            ).orEmpty()

        isCustom =
            args?.getBoolean(
                "isCustom",
                false
            ) ?: false

        imagePath =
            args?.getString(
                "imagePath"
            ).orEmpty()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentFakeIncomingCallBinding.bind(
                view
            )

        binding.tvName.text =
            heroName

        /*
         * User camera.
         */
        isCameraOn = true

        startCamera()

        binding.cardVideoOff.setOnClickListener {

            isCameraOn =
                !isCameraOn

            if (isCameraOn) {

                binding.camerIcon.setImageResource(
                    R.drawable.ic_video_off
                )

                binding.cameraTv.text =
                    "Turn off your video"

                startCamera()

            } else {

                binding.camerIcon.setImageResource(
                    R.drawable.ic_video_call_new
                )

                binding.cameraTv.text =
                    "Turn on your video"

                stopCamera()
            }
        }

        loadVideoThumbnail()

        startIncomingCall()

        startArrowAnimation()

        setupAcceptButton()

        setupDeclineButton()

        setupMessageButton()

        setInitialPositions()

        disableBackPress()
    }

    // =========================================================
    // INCOMING CALL
    // =========================================================

    private fun startIncomingCall() {

        vibrator =
            requireContext()
                .getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            vibrator?.vibrate(
                VibrationEffect
                    .createWaveform(
                        longArrayOf(
                            0,
                            1000,
                            1000
                        ),
                        0
                    )
            )

        } else {

            @Suppress("DEPRECATION")

            vibrator?.vibrate(
                longArrayOf(
                    0,
                    1000,
                    1000
                ),
                0
            )
        }

        val ringtoneUri =
            RingtoneManager
                .getDefaultUri(
                    RingtoneManager.TYPE_RINGTONE
                )

        ringtone =
            RingtoneManager.getRingtone(
                requireContext(),
                ringtoneUri
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.P
        ) {

            ringtone?.isLooping =
                true
        }

        ringtone?.play()
    }

    private fun stopIncomingCall() {

        vibrator?.cancel()

        if (
            ringtone?.isPlaying == true
        ) {

            ringtone?.stop()
        }

        ringtone = null
        vibrator = null
    }

    // =========================================================
    // THUMBNAIL
    // =========================================================

    private fun loadVideoThumbnail() {

        if (isCustom) {

            val filePath =
                imagePath

            if (
                filePath.isNotEmpty() &&
                File(filePath).exists()
            ) {

                Glide.with(
                    requireContext()
                )
                    .load(
                        File(filePath)
                    )
                    .centerCrop()
                    .placeholder(
                        R.drawable.loading
                    )
                    .error(
                        R.drawable.loading
                    )
                    .into(
                        binding.ivCaller
                    )

            } else {

                Glide.with(
                    requireContext()
                )
                    .load(videoUrl)
                    .centerCrop()
                    .placeholder(
                        R.drawable.loading
                    )
                    .error(
                        R.drawable.loading
                    )
                    .thumbnail(0.1f)
                    .into(
                        binding.ivCaller
                    )
            }

        } else {

            Glide.with(
                requireContext()
            )
                .load(videoUrl)
                .centerCrop()
                .placeholder(
                    R.drawable.loading
                )
                .error(
                    R.drawable.loading
                )
                .thumbnail(0.1f)
                .into(
                    binding.ivCaller
                )
        }
    }

    // =========================================================
    // ACCEPT
    // =========================================================

    private fun setupAcceptButton() {

        binding.ivSwipeAccept
            .setOnTouchListener { view, event ->

                val button =
                    view

                val container =
                    binding.swipeContainer

                when (
                    event.action
                ) {

                    MotionEvent.ACTION_DOWN -> {

                        dY =
                            button.y -
                                    event.rawY

                        true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        val newY =
                            event.rawY +
                                    dY

                        val minY =
                            0f

                        val maxY =
                            container.height -
                                    button.height
                                        .toFloat()

                        button.y =
                            newY.coerceIn(
                                minY,
                                maxY
                            )

                        true
                    }

                    MotionEvent.ACTION_UP -> {

                        val maxDistance =
                            container.height -
                                    button.height

                        val progress =
                            if (
                                maxDistance > 0
                            ) {

                                1f -
                                        (
                                                button.y /
                                                        maxDistance
                                                )

                            } else {
                                0f
                            }

                        if (
                            progress > 0.8f
                        ) {

                            stopIncomingCall()

                            findNavController()
                                .navigateSafe(

                                    R.id
                                        .action_fakeIncomingCallFragment_to_videoCallFragment,

                                    bundleOf(

                                        "delay" to delay,

                                        "name" to
                                                heroName,

                                        "videoUrl" to
                                                videoUrl,

                                        "audioUrl" to
                                                audioUrl,

                                        "isCustom" to
                                                isCustom,

                                        "imagePath" to
                                                imagePath
                                    )
                                )

                        } else {

                            resetButtonPosition(
                                button,
                                container
                            )
                        }

                        true
                    }

                    else -> false
                }
            }
    }

    // =========================================================
    // DECLINE
    // =========================================================

    private fun setupDeclineButton() {

        binding.ivDecline
            .setOnTouchListener { view, event ->

                val button =
                    view

                val container =
                    binding.declineContainer

                when (
                    event.action
                ) {

                    MotionEvent.ACTION_DOWN -> {

                        dY =
                            button.y -
                                    event.rawY

                        true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        val newY =
                            event.rawY +
                                    dY

                        val minY =
                            0f

                        val maxY =
                            container.height -
                                    button.height
                                        .toFloat()

                        button.y =
                            newY.coerceIn(
                                minY,
                                maxY
                            )

                        true
                    }

                    MotionEvent.ACTION_UP -> {

                        val maxDistance =
                            container.height -
                                    button.height

                        val progress =
                            if (
                                maxDistance > 0
                            ) {

                                1f -
                                        (
                                                button.y /
                                                        maxDistance
                                                )

                            } else {
                                0f
                            }

                        if (
                            progress > 0.8f
                        ) {

                            stopIncomingCall()

                            findNavController()
                                .popBackStack()

                        } else {

                            resetButtonPosition(
                                button,
                                container
                            )
                        }

                        true
                    }

                    else -> false
                }
            }
    }

    // =========================================================
    // MESSAGE
    // =========================================================

    private fun setupMessageButton() {

        binding.ivMessage
            .setOnTouchListener { view, event ->

                val button =
                    view

                val container =
                    binding.messageContainer

                when (
                    event.action
                ) {

                    MotionEvent.ACTION_DOWN -> {

                        dY =
                            button.y -
                                    event.rawY

                        true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        val newY =
                            event.rawY +
                                    dY

                        val minY =
                            0f

                        val maxY =
                            container.height -
                                    button.height
                                        .toFloat()

                        button.y =
                            newY.coerceIn(
                                minY,
                                maxY
                            )

                        true
                    }

                    MotionEvent.ACTION_UP -> {

                        val maxDistance =
                            container.height -
                                    button.height

                        val progress =
                            if (
                                maxDistance > 0
                            ) {

                                1f -
                                        (
                                                button.y /
                                                        maxDistance
                                                )

                            } else {
                                0f
                            }

                        if (
                            progress > 0.8f
                        ) {

                            stopIncomingCall()

                            findNavController()
                                .navigate(
                                    R.id
                                        .action_fakeIncomingCallFragment_to_chatFragment,

                                    bundleOf(

                                        "name" to
                                                heroName,

                                        "videoUrl" to
                                                videoUrl,

                                        "isFakeCall" to
                                                true
                                    )
                                )

                        } else {

                            resetButtonPosition(
                                button,
                                container
                            )
                        }

                        true
                    }

                    else -> false
                }
            }
    }

    // =========================================================
    // RESET BUTTON
    // =========================================================

    private fun resetButtonPosition(
        button: View,
        container: View
    ) {

        button.animate()
            .y(
                (
                        container.height -
                                button.height
                        ).toFloat()
            )
            .setDuration(250)
            .start()
    }

    // =========================================================
    // INITIAL POSITIONS
    // =========================================================

    private fun setInitialPositions() {

        binding.swipeContainer.post {

            binding.ivSwipeAccept.y =
                (
                        binding.swipeContainer.height -
                                binding.ivSwipeAccept.height
                        ).toFloat()
        }

        binding.declineContainer.post {

            binding.ivDecline.y =
                (
                        binding.declineContainer.height -
                                binding.ivDecline.height
                        ).toFloat()
        }

        binding.messageContainer.post {

            binding.ivMessage.y =
                (
                        binding.messageContainer.height -
                                binding.ivMessage.height
                        ).toFloat()
        }
    }

    // =========================================================
    // ARROW ANIMATION
    // =========================================================

    private fun startArrowAnimation() {

        _binding
            ?.layoutArrows
            ?.animate()
            ?.translationY(-25f)
            ?.alpha(0.2f)
            ?.setDuration(600)
            ?.withEndAction {

                if (
                    _binding == null ||
                    !isAdded
                ) {
                    return@withEndAction
                }

                binding.layoutArrows
                    .translationY = 0f

                binding.layoutArrows
                    .alpha = 1f

                startArrowAnimation()
            }
            ?.start()
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun startCamera() {

        if (!isAdded ||
            _binding == null
        ) {
            return
        }

        binding.previewView.visibility =
            View.VISIBLE

        val future =
            ProcessCameraProvider
                .getInstance(
                    requireContext()
                )

        future.addListener({

            if (!isAdded ||
                _binding == null
            ) {
                return@addListener
            }

            try {

                cameraProvider =
                    future.get()

                val preview =
                    Preview.Builder()
                        .build()
                        .also {

                            it.surfaceProvider =
                                binding.previewView
                                    .surfaceProvider
                        }

                cameraProvider?.unbindAll()

                cameraProvider?.bindToLifecycle(

                    viewLifecycleOwner,

                    CameraSelector
                        .DEFAULT_FRONT_CAMERA,

                    preview
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Camera error",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(
            requireContext()
        ))
    }

    private fun stopCamera() {

        cameraProvider?.unbindAll()

        cameraProvider = null

        if (_binding != null) {

            binding.previewView.visibility =
                View.GONE
        }
    }

    override fun onDestroyView() {

        stopIncomingCall()

        stopCamera()

        _binding = null

        super.onDestroyView()
    }
}

// =========================================================
// SAFE NAVIGATION
// =========================================================

fun NavController.navigateSafe(
    @IdRes actionId: Int,
    args: Bundle? = null
) {

    currentDestination
        ?.getAction(actionId)
        ?.let {

            navigate(
                actionId,
                args
            )
        }
}