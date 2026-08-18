package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.manager.CallAlarmManager
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.databinding.FragmentScheduleCallBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.ScheduleHeroAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponseSchedule
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.ScheduleCallViewModel
import com.axis.vpn.tools.prankvideocall.utils.extension.copyFileToAppStorage
import com.axis.vpn.tools.prankvideocall.utils.extension.getFileName
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.getValue


class ScheduleCallFragment : Fragment() {
    private var _binding: FragmentScheduleCallBinding? = null
    private val binding get() = _binding!!
    private var countDownTimer: CountDownTimer? = null
    private lateinit var callAlarmManager: CallAlarmManager

    private val viewModel: ScheduleCallViewModel by viewModels()
    private val homeDataViewModel: HomeDataViewModel by activityViewModel()
    private var selectedDuration = "1"
    private var selectedRingtone = "Default (Marimba)"
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var heroName = ""
    private var videoUrl = ""
    private var audioUrl = ""
    private var heroImage = 0
    private var delay = 0
    private var customTime = "00:00:00"

    // NEW: Separate ringtone variables
    private var confirmedRingtone = "Default (Marimba)"
    private var tempSelectedRingtone = confirmedRingtone
    private var imagePath: String? = null
    private var videoPath: String? = null
    private var audioPath: String? = null
    private var selectedRingtoneUri: String? = null
    private val ringtonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }

                uri?.let {
                    val ringtone = RingtoneManager.getRingtone(requireContext(), it)
                    binding.ringtoneValue.text = ringtone.getTitle(requireContext())

                    // Save it if you want to use it later
                    selectedRingtoneUri = it.toString()
                }
            }
        }
    // ============ IMAGE PICKER ============
    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    imagePath = requireContext().copyFileToAppStorage(it, "image")
                    if (imagePath != null) {
                        binding.ivProfile.setImageURI(Uri.parse(imagePath))
                        Log.d("IMAGE_PICKER", "Image saved to: $imagePath")
                    }
                } catch (e: Exception) {
                    Log.e("IMAGE_PICKER", "Failed to copy image", e)
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    // ============ VIDEO PICKER ============
    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    videoPath = requireContext().copyFileToAppStorage(it, "video")
                    if (videoPath != null) {
                        binding.tvVideo.text = requireContext().getFileName(it)
                        Log.d("VIDEO_PICKER", "Video saved to: $videoPath")
                    }
                } catch (e: Exception) {
                    Log.e("VIDEO_PICKER", "Failed to copy video", e)
                    Toast.makeText(requireContext(), "Failed to save video", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    // ============ AUDIO PICKER ============
    private val audioPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    audioPath =requireContext().copyFileToAppStorage(it, "audio")
                    if (audioPath != null) {
                        binding.tvAudio.text = requireContext().getFileName(it)
                        Log.d("AUDIO_PICKER", "Audio saved to: $audioPath")
                    }
                } catch (e: Exception) {
                    Log.e("AUDIO_PICKER", "Failed to copy audio", e)
                    Toast.makeText(requireContext(), "Failed to save audio", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    // ============ LIFECYCLE ============
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callAlarmManager = CallAlarmManager(requireContext())

        parentFragmentManager.setFragmentResultListener(
            "schedule_data",
            viewLifecycleOwner
        ) { _, bundle ->
            heroName = bundle.getString("name", "")
            videoUrl = bundle.getString("videoUrl", "")
            audioUrl = bundle.getString("audioUrl", "")
            heroImage = bundle.getInt("image", 0)
            delay = bundle.getInt("delay", 0)
            customTime = bundle.getString("time", "")
            setHeroData()
        }

        setupHeroesRecyclerView()
        setupClickListeners()
        setDefaultDateTime()
        setHeroData()
    }

    // ============ RECYCLER VIEW SETUP ============
    private fun setupHeroesRecyclerView() {
        homeDataViewModel.scheduleVideos.observe(viewLifecycleOwner) { list ->
            setupHeroes(list)
        }
    }

    private fun setupHeroes(list: List<PrankVideoResponseSchedule>) {
        binding.rvHeroesSchedule.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )

            adapter = ScheduleHeroAdapter(
                list = list,
                onClick = { item ->
                    scheduleCallFromRecycler(item)
                },
                onDelete = {
                    callAlarmManager.cancelCall()
                    Toast.makeText(
                        requireContext(),
                        "Scheduled call cancelled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
    // ============ CLICK LISTENERS ============
    private fun setupClickListeners() {
        // Image picker
        binding.btnSelectImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Video picker
        binding.btnSelectVideo.setOnClickListener {
            videoPicker.launch("video/*")
        }

        // Audio picker
        binding.btnSelectAudio.setOnClickListener {
            audioPicker.launch("audio/*")
        }

        // Duration buttons
        binding.btn1Min.setOnClickListener {
            selectDuration(binding.btn1Min, "1")
        }

        binding.btn5Min.setOnClickListener {
            selectDuration(binding.btn5Min, "5")
        }

        binding.btn15Min.setOnClickListener {
            selectDuration(binding.btn15Min, "15")
        }
        binding.btnCustomDuration?.setOnClickListener {
            selectDuration(it as TextView, "custom")
        }
        // Date and Time pickers
        binding.tvDate.setOnClickListener {
            if (selectedDuration == "custom") {
                showDatePicker()
            } else {

            }
        }

        binding.tvTime.setOnClickListener {
            if (selectedDuration == "custom") {
                showTimePicker()
            } else {

            }
        }
        // Ringtone picker
        binding.changeButton.setOnClickListener {
            showRingtonePicker()
        }

        // Schedule button
        binding.btnScheduleCall.setOnClickListener {
            scheduleCall()
        }

        // RecyclerView touch handling
        binding.rvHeroesSchedule.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        rv.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    // ============ HERO DATA DISPLAY ============
    private fun setHeroData() {
        // No scheduled call
        if (heroName.isEmpty()) {
            binding.upcommingCallCard.visibility = View.GONE
            return
        }

        // Scheduled call exists
        binding.upcommingCallCard.visibility = View.VISIBLE
        binding.btnSchedule.setBackgroundResource(R.drawable.btn_duration_selected_green)
        binding.tvHeroName.text = heroName

        binding.ivDelete.setOnClickListener {
            callAlarmManager.cancelCall()
            binding.upcommingCallCard.visibility = View.GONE
            heroName = "" // Reset hero name
        }

        Glide.with(requireContext())
            .load(videoUrl)
            .centerCrop()
            .thumbnail(0.1f)
            .into(binding.ivHero)

        if (delay > 0) {
            Log.d("CallTimer", "Starting countdown")
            startCountDown(delay.toLong())
        } else {
            Log.d("CallTimer", "Showing custom time: $customTime")
            binding.tvCallTime.text = customTime
        }
    }

    // ============ COUNTDOWN TIMER ============
    private fun startCountDown(delayInMillis: Long) {
        countDownTimer?.cancel()

        Log.d("CallTimer", "Countdown started: ${delayInMillis} ms")

        countDownTimer = object : CountDownTimer(delayInMillis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000

                val hrs = totalSeconds / 3600
                val mins = (totalSeconds % 3600) / 60
                val secs = totalSeconds % 60

                val time = String.format("%02d:%02d:%02d", hrs, mins, secs)

                Log.d(
                    "CallTimer",
                    "millisUntilFinished=$millisUntilFinished, totalSeconds=$totalSeconds, time=$time"
                )

                binding.tvCallTime.text = time
            }

            override fun onFinish() {
                Log.d("CallTimer", "Countdown finished")
                binding.tvCallTime.text = "00:00:00"
            }
        }

        countDownTimer?.start()
    }

    // ============ DURATION SELECTION ============
    private fun selectDuration(button: TextView, duration: String) {
        binding.btn1Min.isSelected = false
        binding.btn5Min.isSelected = false
        binding.btn15Min.isSelected = false
        binding.btnCustomDuration.isSelected = false

        button.isSelected = true
        selectedDuration = duration

        updateDurationButtons()

        if (duration != "custom") {
            val calendar = Calendar.getInstance()

            when (duration) {
                "1" -> calendar.add(Calendar.MINUTE, 1)
                "5" -> calendar.add(Calendar.MINUTE, 5)
                "15" -> calendar.add(Calendar.MINUTE, 15)
            }

            updateTimeFields(calendar)
        }
    }

    // ============ DATE & TIME MANAGEMENT ============
    private fun setDefaultDateTime() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1) // Default to 1 minute from now
        updateTimeFields(calendar)
    }

    private fun updateTimeFields(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("hh:mmaa", Locale.US)

        selectedDate = dateFormat.format(calendar.time)
        selectedTime = timeFormat.format(calendar.time).uppercase()

        binding.tvDate.text = selectedDate
        binding.tvTime.text = selectedTime
    }

    // ============ DATE PICKER ============
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedDate != null) {
            try {
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                calendar.time = sdf.parse(selectedDate!!) ?: Calendar.getInstance().time
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                selectedDate = dateFormat.format(calendar.time)
                binding.tvDate.text = selectedDate

                // Reset duration selection since custom date is picked
                selectedDuration = "custom"
                updateDurationButtons()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ============ TIME PICKER ============
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        if (selectedTime != null) {
            try {
                val sdf = SimpleDateFormat("hh:mmaa", Locale.US)
                val date = sdf.parse(selectedTime!!)
                val tempCalendar = Calendar.getInstance()
                tempCalendar.time = date
                hour = tempCalendar.get(Calendar.HOUR_OF_DAY)
                minute = tempCalendar.get(Calendar.MINUTE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                val timeFormat = SimpleDateFormat("hh:mmaa", Locale.US)
                selectedTime = timeFormat.format(calendar.time).uppercase()
                binding.tvTime.text = selectedTime

                // Reset duration selection since custom time is picked
                selectedDuration = "custom"
                updateDurationButtons()
            },
            hour,
            minute,
            false
        ).show()
    }

    // ============ RINGTONE PICKER ============
    private fun showRingtonePicker() {

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_RINGTONE
            )

            putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                true
            )

            putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                false
            )
        }

        ringtonePickerLauncher.launch(intent)
    }
    // ============ UPDATE BUTTON STATES ============
    private fun updateDurationButtons() {

        val selectedBg = R.drawable.btn_duration_selected
        val unselectedBg = R.drawable.btn_duration_unselected

        // Backgrounds
        binding.btn1Min.setBackgroundResource(
            if (selectedDuration == "1") selectedBg else unselectedBg
        )

        binding.btn5Min.setBackgroundResource(
            if (selectedDuration == "5") selectedBg else unselectedBg
        )

        binding.btn15Min.setBackgroundResource(
            if (selectedDuration == "15") selectedBg else unselectedBg
        )

        binding.btnCustomDuration.setBackgroundResource(
            if (selectedDuration == "custom") selectedBg else unselectedBg
        )

        // Text colors
        binding.btn1Min.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selectedDuration == "1") android.R.color.white
                else android.R.color.black
            )
        )

        binding.btn5Min.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selectedDuration == "5") android.R.color.white
                else android.R.color.black
            )
        )

        binding.btn15Min.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selectedDuration == "15") android.R.color.white
                else android.R.color.black
            )
        )

        binding.btnCustomDuration.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selectedDuration == "custom") android.R.color.white
                else android.R.color.black
            )
        )

        // Enable date & time only when custom is selected
        val isCustom = selectedDuration == "custom"

        binding.tvDate.isEnabled = isCustom
        binding.tvTime.isEnabled = isCustom

        binding.tvDate.alpha = if (isCustom) 1f else 0.5f
        binding.tvTime.alpha = if (isCustom) 1f else 0.5f
    }


    // ============ SCHEDULE CALL ============
    private fun scheduleCall() {
        val name = binding.etName.text.toString().trim()

        // Validate name
        if (name.isEmpty()) {
            binding.etName.error = "Enter Name"
            return
        }

        // Validate image
        if (imagePath == null) {
            Toast.makeText(requireContext(), "Select Image", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate video
        if (videoPath == null) {
            Toast.makeText(requireContext(), "Select Video", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate date and time
        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(requireContext(), "Select Date and Time", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Parse the selected date and time
            val format = SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US)
            val scheduleDateTime = "$selectedDate $selectedTime"

            Log.d("SCHEDULE_CALL", "Attempting to parse: $scheduleDateTime")

            val scheduleDate = format.parse(scheduleDateTime)
            if (scheduleDate == null) {
                Toast.makeText(requireContext(), "Invalid date/time format", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            // Calculate delay in milliseconds

            val delayInMillis = getDelayInMillis()

            if (delayInMillis <= 0) {
                Toast.makeText(
                    requireContext(),
                    "Please select a future date and time.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }


            // Save caller data
            saveCaller(name)

            Log.d("SCHEDULE_CALL", "Delay in millis: $delayInMillis")
            Log.d("SCHEDULE_CALL", "Hero Name: $heroName")
            Log.d("SCHEDULE_CALL", "Video URL: $videoUrl")
            Log.d("SCHEDULE_CALL", "Audio URL: $audioUrl")

            // Schedule the call
            callAlarmManager.scheduleCall(
                delayInMillis = delayInMillis,
                heroName = heroName,
                videoUrl = videoUrl,
                audioUrl = audioUrl
            )

            // Show success message
            Toast.makeText(
                requireContext(),
                "Call scheduled for $selectedDate at $selectedTime",
                Toast.LENGTH_LONG
            ).show()

            // Update UI with scheduled call

            setHeroData()
            startCountDown(delayInMillis.toLong())

            // Reset form
            resetForm()

        } catch (e: Exception) {
            Log.e("SCHEDULE_CALL", "Error scheduling call: ${e.message}", e)
            Toast.makeText(
                requireContext(),
                "Error scheduling call: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getDelayInMillis(): Long {
        return when (selectedDuration) {
            "1" -> 1 * 60 * 1000L
            "5" -> 5 * 60 * 1000L
            "15" -> 15 * 60 * 1000L
            else -> {
                val format = SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.US)
                val scheduleDate =
                    format.parse("$selectedDate $selectedTime")
                        ?: return -1

                scheduleDate.time - System.currentTimeMillis()
            }
        }
    }

    // ============ SCHEDULE FROM RECYCLER ============
    private fun scheduleCallFromRecycler(item: PrankVideoResponseSchedule) {

        val delayInMillis = item.seconds * 1000L

        callAlarmManager.scheduleCall(
            delayInMillis = delayInMillis,
            heroName = item.name,
            videoUrl = item.videoUrl,
            audioUrl = item.audioUrl
        )


        Toast.makeText(
            requireContext(),
            "${item.name} will call you in ${item.seconds} seconds.",
            Toast.LENGTH_SHORT
        ).show()


    }


    // ============ SAVE CALLER DATA ============
    private fun saveCaller(name: String) {
        heroName = name
        audioUrl = audioPath ?: ""
        videoUrl = videoPath ?: ""

        Log.d("SAVE_CALLER", "Name: $heroName, Audio: $audioUrl, Video: $videoUrl")
    }

    // ============ RESET FORM ============
    private fun resetForm() {
        binding.etName.text?.clear()
        binding.etName.clearFocus()
        imagePath = null
        videoPath = null
        audioPath = null
        binding.tvVideo.text = "Select Video"
        binding.tvAudio.text = "Select Custom"
        setDefaultDateTime()
    }




    // ============ CLEANUP ============
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}