package com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.manager.CallAlarmManager
import com.axis.vpn.tools.prankvideocall.databinding.BottomSheetCallBinding
import com.axis.vpn.tools.prankvideocall.utils.constants.VideoCacheManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
interface OnScheduleCallListener {
    fun onScheduleCall(
        heroName: String,
        videoUrl: String,
        delay: Int,
        isCustom: Boolean,
        time: String
    )
}

class HeroCallBottomSheet : BottomSheetDialogFragment() {

    private var heroName = ""
    private var videoUrl = ""
    private var audioUrl = ""
    private var isCustom = false
    private var imagePath = ""
    private var isCustomTimeSelected = false

    private var _binding: BottomSheetCallBinding? = null
    private val binding get() = _binding!!

    private var selectedDelay = 0

    private lateinit var callAlarmManager: CallAlarmManager
    private var listener: OnScheduleCallListener? = null

    // App-scoped — survives dismiss(), unlike viewLifecycleOwner.lifecycleScope
    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun setOnScheduleCallListener(listener: OnScheduleCallListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        isCustom = args.getBoolean("isCustom", false)
        imagePath = args.getString("imagePath", "")
        heroName = args.getString("name", "")
        videoUrl = args.getString("videoUrl", "")
        audioUrl = args.getString("audioUrl", "")
        callAlarmManager = CallAlarmManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectDelay(0)

        binding.card10.setOnClickListener {
            isCustomTimeSelected = false
            selectDelay(0)
        }
        binding.card30.setOnClickListener {
            isCustomTimeSelected = false
            selectDelay(10)
        }
        binding.card45.setOnClickListener {
            isCustomTimeSelected = false
            selectDelay(30)
        }
        binding.card60.setOnClickListener {
            isCustomTimeSelected = false
            selectDelay(60)
        }

        binding.editTime.setOnClickListener { showTimePicker() }
        binding.btnCallNow.setOnClickListener { handleCall() }
    }

    // =========================================================
    // MAIN CALL HANDLER
    // =========================================================
    private fun handleCall() {

        if (isCustomTimeSelected) {
            handleCustomTime()
            return
        }

        // CALL NOW
        if (selectedDelay == 0) {
            Toast.makeText(requireContext(), "Starting call...", Toast.LENGTH_SHORT).show()
            startBackgroundCaching()
            navigateToFakeCall()
            dismiss()
            return
        }

        // DELAYED CALL - 10 / 30 / 60 SECONDS
        val delayMillis = selectedDelay * 1000L

        startBackgroundCaching()

        val scheduled = callAlarmManager.scheduleCall(
            delayInMillis = delayMillis,
            heroName = heroName,
            videoUrl = videoUrl,
            audioUrl = audioUrl
        )

        if (!scheduled) {
            Toast.makeText(
                requireContext(),
                "Please allow 'Alarms & reminders' permission to schedule calls",
                Toast.LENGTH_LONG
            ).show()
            return  // don't dismiss/toast success if scheduling actually failed
        }

        Toast.makeText(
            requireContext(),
            "Call will start in $selectedDelay sec",
            Toast.LENGTH_SHORT
        ).show()

        listener?.onScheduleCall(
            heroName = heroName,
            videoUrl = videoUrl,
            delay = delayMillis.toInt(),
            isCustom = false,
            time = ""
        )

        dismiss()
    }

    // =========================================================
    // BACKGROUND VIDEO CACHE
    // =========================================================
    private fun startBackgroundCaching() {
        val context = context ?: return
        if (videoUrl.isBlank()) return

        cacheScope.launch {
            try {
                VideoCacheManager.ensureVideoCached(context, videoUrl)
                Log.d("HeroCallCache", "Background download finished")
            } catch (e: Exception) {
                Log.e("HeroCallCache", "Background download failed", e)
            }
        }
    }

    // =========================================================
    // CUSTOM TIME
    // =========================================================
    private fun handleCustomTime() {
        val timeText = binding.etTime.text.toString()

        if (!timeText.contains(":")) {
            Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show()
            return
        }

        val parts = timeText.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return
        val context = context ?: return

        cacheScope.launch {
            withContext(Dispatchers.Main) {
                if (isAdded && _binding != null) {
                    binding.btnCallNow.isEnabled = false
                    Toast.makeText(context, "Preparing video...", Toast.LENGTH_SHORT).show()
                }
            }

            val localFile = VideoCacheManager.ensureVideoCached(context, videoUrl)

            withContext(Dispatchers.Main) {
                if (localFile == null) {
                    if (isAdded && _binding != null) {
                        binding.btnCallNow.isEnabled = true
                        Toast.makeText(context, "Unable to download video", Toast.LENGTH_LONG).show()
                    }
                    return@withContext
                }

                val scheduled = callAlarmManager.scheduleCallAtTime(
                    hour = hour,
                    minute = minute,
                    heroName = heroName,
                    videoUrl = localFile.absolutePath,
                    audioUrl = audioUrl
                )

                if (!scheduled) {
                    if (isAdded && _binding != null) {
                        binding.btnCallNow.isEnabled = true
                        Toast.makeText(
                            context,
                            "Please allow 'Alarms & reminders' permission to schedule calls",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@withContext
                }

                val formattedTime = String.format("%02d:%02d", hour, minute)

                listener?.onScheduleCall(
                    heroName,
                    localFile.absolutePath,
                    0,
                    true,
                    formattedTime
                )

                if (isAdded && _binding != null) {
                    binding.btnCallNow.isEnabled = true
                    Toast.makeText(context, "Call scheduled successfully.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    private fun navigateToFakeCall() {
        try {
            val navController =
                requireActivity().findNavController(R.id.fragmentContainerView)

            navController.navigate(
                R.id.fakeIncomingCallFragment,
                bundleOf(
                    "name" to heroName,
                    "videoUrl" to videoUrl,
                    "audioUrl" to audioUrl,
                    "isCustom" to isCustom,
                    "imagePath" to imagePath,
                    "delay" to selectedDelay.toLong()
                )
            )
        } catch (e: Exception) {
            Log.e("HeroCallBottomSheet", "Navigation failed", e)
        }
    }

    // =========================================================
    // TIME PICKER
    // =========================================================
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        val dialog = TimePickerDialog(
            requireContext(),
            R.style.CustomTimePickerTheme,
            { _, hour, minute ->
                binding.etTime.setText(String.format("%02d:%02d", hour, minute))
                isCustomTimeSelected = true
                setUnSelected(binding.card10)
                setUnSelected(binding.card30)
                setUnSelected(binding.card45)
                setUnSelected(binding.card60)
                selectedDelay = -1
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        dialog.show()
    }

    // =========================================================
    // DELAY SELECTION UI
    // =========================================================
    private fun selectDelay(delay: Int) {
        selectedDelay = delay
        setUnSelected(binding.card10)
        setUnSelected(binding.card30)
        setUnSelected(binding.card45)
        setUnSelected(binding.card60)

        when (delay) {
            0 -> setSelected(binding.card10)
            10 -> setSelected(binding.card30)
            30 -> setSelected(binding.card45)
            60 -> setSelected(binding.card60)
        }
    }

    private fun setSelected(card: MaterialCardView) {
        card.strokeWidth = 4
        val layout = card.getChildAt(0) as LinearLayout
        layout.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_selected)
        for (i in 0 until layout.childCount) {
            (layout.getChildAt(i) as? TextView)?.setTextColor(Color.WHITE)
        }
    }

    private fun setUnSelected(card: MaterialCardView) {
        card.strokeWidth = 0
        val layout = card.getChildAt(0) as LinearLayout
        layout.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected)
        for (i in 0 until layout.childCount) {
            (layout.getChildAt(i) as? TextView)?.setTextColor(Color.BLACK)
        }
    }

    override fun onDestroyView() {
        listener = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(
            name: String,
            videoUrl: String,
            isCustom: Boolean = false,
            imagePath: String = "",
            audioUrl: String = ""
        ): HeroCallBottomSheet {
            return HeroCallBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("name", name)
                    putString("videoUrl", videoUrl)
                    putBoolean("isCustom", isCustom)
                    putString("imagePath", imagePath)
                    putString("audioUrl", audioUrl)
                }
            }
        }
    }
}
