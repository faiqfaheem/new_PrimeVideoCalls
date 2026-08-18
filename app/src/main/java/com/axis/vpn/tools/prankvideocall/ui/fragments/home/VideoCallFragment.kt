package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentVideoCallBinding
import com.axis.vpn.tools.prankvideocall.ui.activity.StartActivity
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet.CallEndBottomSheet
import com.axis.vpn.tools.prankvideocall.utils.base.activity.ParentActivity
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.constants.VideoCacheManager
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//import jp.wasabeef.glide.transformations.BlurTransformation

class VideoCallFragment :
    Fragment() {

    private lateinit var binding:
            FragmentVideoCallBinding

    private var heroName = ""

    private var delay = 0L

    private var videoUrl = ""

    private var audioUrl = ""

    private var imagePath = ""

    private var isCustom = false

    private var isMicMuted = false

    private var isSpeakerOn = true

    private var isVideoEnabled = true

    private var audioPlayer:
            ExoPlayer? = null

    private var exoPlayer:
            ExoPlayer? = null

    private var lensFacing =
        CameraSelector.LENS_FACING_FRONT

    private var cameraProvider:
            ProcessCameraProvider? = null

    private lateinit var audioManager:
            AudioManager

    private var seconds = 0

    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    private var cacheJob:
            Job? = null

    private var isVideoReady =
        false

    private var isVideoLoading =
        false

    private val timerRunnable =
        object : Runnable {

            override fun run() {

                if (!isAdded) {
                    return
                }

                seconds++

                val min =
                    seconds / 60

                val sec =
                    seconds % 60

                binding.tvDuration.text =
                    String.format(
                        "%02d:%02d",
                        min,
                        sec
                    )

                handler.postDelayed(
                    this,
                    1000
                )
            }
        }

    // =========================================================
    // CREATE
    // =========================================================

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
                "imagePath",
                ""
            ).orEmpty()
    }

    // =========================================================
    // VIEW
    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentVideoCallBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        audioManager =
            requireContext()
                .getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

        disableBackPress()

        binding.tvName.text =
            heroName

        /*
         * Show thumbnail while video is loading.
         */
        loadCallerImage()

        /*
         * Blur background.
         */
        loadBlurThumbnail()

        /*
         * Camera preview.
         */
        binding.previewView.apply {

            implementationMode =
                androidx.camera.view
                    .PreviewView
                    .ImplementationMode
                    .COMPATIBLE

            scaleType =
                androidx.camera.view
                    .PreviewView
                    .ScaleType
                    .FILL_CENTER
        }

        /*
         * Initially:
         *
         * connectingLayout = VISIBLE
         * mainLayout = GONE
         */
        showConnecting()

        setupVideo()

        startTimer()

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()
        }

        setupButtons()

        makePreviewDraggable()
    }

    // =========================================================
    // IMAGE
    // =========================================================

    private fun loadCallerImage() {

        /*
         * For custom caller image.
         */
        if (
            isCustom &&
            imagePath.isNotEmpty() &&
            File(imagePath).exists()
        ) {

            Glide.with(
                requireContext()
            )
                .load(
                    File(imagePath)
                )
                .centerCrop()
                .into(
                    binding.ivCallerImage
                )

            return
        }

        /*
         * Normal hero.
         */
        Glide.with(
            requireContext()
        )
            .load(videoUrl)
            .centerCrop()
            .thumbnail(0.1f)
            .into(
                binding.ivCallerImage
            )
    }

    private fun loadBlurThumbnail() {

        if (
            isCustom &&
            imagePath.isNotEmpty() &&
            File(imagePath).exists()
        ) {

            Glide.with(
                requireContext()
            )
                .load(
                    File(imagePath)
                )
                .apply(
                    com.bumptech.glide.request
                        .RequestOptions
                        .bitmapTransform(
                            BlurTransformation(
                                25,
                                3
                            )
                        )
                )
                .into(
                    binding.thumbnailConnecting
                )

        } else {

            Glide.with(
                requireContext()
            )
                .load(videoUrl)
                .apply(
                    com.bumptech.glide.request
                        .RequestOptions
                        .bitmapTransform(
                            BlurTransformation(
                                25,
                                3
                            )
                        )
                )
                .into(
                    binding.thumbnailConnecting
                )
        }
    }

    // =========================================================
    // VIDEO SETUP
    // =========================================================

    @OptIn(UnstableApi::class)
    private fun setupVideo() {

        exoPlayer =
            ExoPlayer.Builder(
                requireContext()
            ).build()

        binding.playerView.player =
            exoPlayer

        binding.playerView.useController =
            false

        binding.playerView.controllerAutoShow =
            false

        /*
         * DO NOT directly set videoUrl here.
         *
         * First check cache.
         */
        loadCachedVideo()
    }

    // =========================================================
    // CACHE / DOWNLOAD
    // =========================================================

    private fun loadCachedVideo() {

        if (
            videoUrl.isBlank()
        ) {

            Log.e(
                "VideoCall",
                "videoUrl is empty"
            )

            return
        }

        val context =
            context ?: return

        cacheJob?.cancel()

        cacheJob =
            viewLifecycleOwner
                .lifecycleScope
                .launch {

                    showConnecting()

                    isVideoLoading =
                        true

                    /*
                     * FIRST:
                     * Check local cache.
                     */
                    var localFile =
                        withContext(
                            Dispatchers.IO
                        ) {

                            VideoCacheManager
                                .getCachedVideo(
                                    context,
                                    videoUrl
                                )
                        }

                    if (
                        localFile != null
                    ) {

                        Log.d(
                            "VideoCall",
                            "VIDEO FOUND IN CACHE"
                        )

                    } else {

                        /*
                         * Not downloaded yet.
                         */
                        Log.d(
                            "VideoCall",
                            "VIDEO NOT CACHED"
                        )

                        /*
                         * Keep connecting UI.
                         */
                        showConnecting()

                        /*
                         * Download.
                         */
                        localFile =
                            withContext(
                                Dispatchers.IO
                            ) {

                                VideoCacheManager
                                    .ensureVideoCached(
                                        context,
                                        videoUrl
                                    )
                            }
                    }

                    if (
                        !isAdded ||
                        !::binding.isInitialized
                    ) {
                        return@launch
                    }

                    isVideoLoading =
                        false

                    /*
                     * Download failed.
                     */
                    if (
                        localFile == null
                    ) {

                        Log.e(
                            "VideoCall",
                            "VIDEO DOWNLOAD FAILED"
                        )

                        Toast.makeText(
                            context,
                            "Unable to load video",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    Log.d(
                        "VideoCall",
                        "LOCAL VIDEO = ${localFile.absolutePath}"
                    )

                    isVideoReady =
                        true

                    /*
                     * Play local file.
                     */
                    playLocalVideo(
                        localFile
                    )
                }
    }

    // =========================================================
    // PLAY LOCAL VIDEO
    // =========================================================

    @OptIn(UnstableApi::class)
    private fun playLocalVideo(
        file: File
    ) {

        if (
            !isAdded ||
            !::binding.isInitialized
        ) {
            return
        }

        val player =
            exoPlayer ?: return

        val mediaItem =
            MediaItem.fromUri(
                Uri.fromFile(file)
            )

        player.stop()

        player.clearMediaItems()

        player.setMediaItem(
            mediaItem
        )

        player.repeatMode =
            Player.REPEAT_MODE_ONE

        player.playWhenReady =
            true

        /*
         * If separate audio exists,
         * mute video's own audio.
         */
        player.volume =
            if (
                audioUrl.isNotEmpty()
            ) {
                0f
            } else {
                1f
            }

        player.prepare()

        /*
         * Don't add multiple listeners.
         */
        player.addListener(
            object : Player.Listener {

                override fun
                        onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_READY
                    ) {

                        showMainUI()
                    }
                }

                override fun
                        onIsPlayingChanged(
                    isPlaying: Boolean
                ) {

                    if (
                        isPlaying
                    ) {

                        showMainUI()
                    }
                }
            }
        )

        /*
         * Separate audio.
         */
        if (
            audioUrl.isNotEmpty()
        ) {

            setupAudioPlayer()
        }
    }

    // =========================================================
    // CUSTOM AUDIO
    // =========================================================

    @OptIn(UnstableApi::class)
    private fun setupAudioPlayer() {

        if (
            audioPlayer != null
        ) {
            return
        }

        audioPlayer =
            ExoPlayer.Builder(
                requireContext()
            ).build()

        val mediaItem =
            MediaItem.fromUri(
                audioUrl
            )

        audioPlayer?.apply {

            setMediaItem(
                mediaItem
            )

            repeatMode =
                Player.REPEAT_MODE_ONE

            playWhenReady =
                true

            volume =
                if (isMicMuted) {
                    0f
                } else {
                    1f
                }

            prepare()
        }
    }

    // =========================================================
    // CONNECTING UI
    // =========================================================

    private fun showConnecting() {

        if (
            !::binding.isInitialized
        ) {
            return
        }

        binding.connectingLayout.visibility =
            View.VISIBLE

        binding.mainLayout.visibility =
            View.GONE
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private fun showMainUI() {

        if (
            !isAdded ||
            !::binding.isInitialized
        ) {
            return
        }

        binding.connectingLayout.visibility =
            View.GONE

        binding.mainLayout.visibility =
            View.VISIBLE

        if (
            isVideoEnabled
        ) {

            binding.playerView.visibility =
                View.VISIBLE

            binding.ivCallerImage.visibility =
                View.GONE
        }
    }

    // =========================================================
    // BUTTONS
    // =========================================================

    private fun setupButtons() {

        binding.btnVideo.setOnClickListener {

            isVideoEnabled =
                !isVideoEnabled

            if (
                isVideoEnabled
            ) {

                binding.btnVideo
                    .setImageResource(
                        R.drawable.ic_video_call_new
                    )

                binding.playerView.visibility =
                    View.VISIBLE

                binding.cardPreview.visibility =
                    View.VISIBLE

                binding.ivCallerImage.visibility =
                    View.GONE

                if (
                    isVideoReady
                ) {

                    exoPlayer?.play()
                }

            } else {

                binding.btnVideo
                    .setImageResource(
                        R.drawable.ic_video_off_new
                    )

                binding.playerView.visibility =
                    View.GONE

                binding.cardPreview.visibility =
                    View.GONE

                binding.ivCallerImage.visibility =
                    View.VISIBLE
            }
        }

        binding.btnMessage.setOnClickListener {

            findNavController().navigate(

                R.id
                    .action_videoCallFragment_to_chatFragment,

                bundleOf(

                    "name" to
                            heroName,

                    "videoUrl" to
                            videoUrl,

                    "isFromVideoCall" to
                            true
                )
            )
        }

        binding.btnSpeaker.setOnClickListener {

            isSpeakerOn =
                !isSpeakerOn

            if (
                isSpeakerOn
            ) {

                binding.btnSpeaker
                    .setImageResource(
                        R.drawable.ic_speaker
                    )

                audioManager
                    .isSpeakerphoneOn =
                    true

            } else {

                binding.btnSpeaker
                    .setImageResource(
                        R.drawable.ic_speaker_off
                    )

                audioManager
                    .isSpeakerphoneOn =
                    false
            }
        }

        binding.btnMic.setOnClickListener {

            isMicMuted =
                !isMicMuted

            if (
                isMicMuted
            ) {

                binding.btnMic
                    .setImageResource(
                        R.drawable.ic_mic_off
                    )

                audioPlayer?.volume =
                    0f

                if (
                    audioPlayer == null
                ) {

                    exoPlayer?.volume =
                        0f
                }

            } else {

                binding.btnMic
                    .setImageResource(
                        R.drawable.ic_mic
                    )

                if (
                    audioUrl.isNotEmpty()
                ) {

                    audioPlayer?.volume =
                        1f

                } else {

                    exoPlayer?.volume =
                        1f
                }
            }
        }

        binding.btnSwitchCamera
            .setOnClickListener {

                lensFacing =
                    if (
                        lensFacing ==
                        CameraSelector
                            .LENS_FACING_FRONT
                    ) {

                        CameraSelector
                            .LENS_FACING_BACK

                    } else {

                        CameraSelector
                            .LENS_FACING_FRONT
                    }

                startCamera()
            }

        binding.btnEndCall
            .setOnClickListener {

                val duration =
                    binding.tvDuration
                        .text
                        .toString()

                stopTimer()

                findNavController()
                    .navigate(
                        R.id
                            .action_videoCallFragment_to_homeFragment
                    )

                CallEndBottomSheet(
                    heroName,
                    videoUrl,
                    duration
                ).show(
                    parentFragmentManager,
                    "CallEndBottomSheet"
                )
            }
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun startTimer() {

        handler.removeCallbacks(
            timerRunnable
        )

        handler.post(
            timerRunnable
        )
    }

    private fun stopTimer() {

        handler.removeCallbacks(
            timerRunnable
        )
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun startCamera() {

        if (
            !isAdded ||
            !::binding.isInitialized
        ) {
            return
        }

        val future =
            ProcessCameraProvider
                .getInstance(
                    requireContext()
                )

        future.addListener({

            if (
                !isAdded ||
                !::binding.isInitialized
            ) {
                return@addListener
            }

            try {

                cameraProvider =
                    future.get()

                val preview =
                    Preview.Builder()
                        .build()

                preview.surfaceProvider =
                    binding.previewView
                        .surfaceProvider

                val selector =
                    CameraSelector.Builder()
                        .requireLensFacing(
                            lensFacing
                        )
                        .build()

                cameraProvider
                    ?.unbindAll()

                cameraProvider
                    ?.bindToLifecycle(

                        viewLifecycleOwner,

                        selector,

                        preview
                    )

            } catch (e: Exception) {

                Log.e(
                    "VideoCall",
                    "Camera error",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(
            requireContext()
        ))
    }

    // =========================================================
    // DRAG CAMERA
    // =========================================================

    private fun makePreviewDraggable() {

        binding.cardPreview
            .setOnTouchListener(

                object :
                    View.OnTouchListener {

                    private var dX =
                        0f

                    private var dY =
                        0f

                    override fun onTouch(
                        view: View,
                        event: MotionEvent
                    ): Boolean {

                        when (
                            event.action
                        ) {

                            MotionEvent.ACTION_DOWN -> {

                                dX =
                                    view.x -
                                            event.rawX

                                dY =
                                    view.y -
                                            event.rawY
                            }

                            MotionEvent.ACTION_MOVE -> {

                                view.animate()
                                    .x(
                                        event.rawX +
                                                dX
                                    )
                                    .y(
                                        event.rawY +
                                                dY
                                    )
                                    .setDuration(
                                        0
                                    )
                                    .start()
                            }
                        }

                        return true
                    }
                }
            )
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onResume() {

        super.onResume()

        exoPlayer?.play()

        audioPlayer?.play()
    }

    override fun onPause() {

        super.onPause()

        exoPlayer?.pause()

        audioPlayer?.pause()
    }

    override fun onDestroyView() {

        cacheJob?.cancel()

        stopTimer()

        exoPlayer?.release()

        exoPlayer = null

        audioPlayer?.release()

        audioPlayer = null

        cameraProvider?.unbindAll()

        cameraProvider = null

        super.onDestroyView()
    }

    companion object {

        fun newInstance(
            name: String,
            videoUrl: String
        ): VideoCallFragment {

            return VideoCallFragment()
                .apply {

                    arguments =
                        Bundle().apply {

                            putString(
                                "name",
                                name
                            )

                            putString(
                                "videoUrl",
                                videoUrl
                            )
                        }
                }
        }
    }
}