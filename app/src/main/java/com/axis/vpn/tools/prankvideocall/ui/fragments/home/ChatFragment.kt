package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.manager.CallAlarmManager
import com.axis.vpn.tools.prankvideocall.data.remote.GroqClient
import com.axis.vpn.tools.prankvideocall.data.remote.GroqRequest
import com.axis.vpn.tools.prankvideocall.databinding.FragmentChatBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.MessageAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet.AiReplyBottomSheet
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.ChatViewModel
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.GroqMessage
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.Message
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var isFakeCall = false
    private var isFromVideoCall = false
    private var userName = ""
    private var userImage = 0

    private var heroName = ""
    private var isAiChat = false
    private lateinit var callAlarmManager: CallAlarmManager
    private var videoUrl = ""
    private var audioUrl = ""
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private var selectedImageUri: Uri? = null
    private val viewModel: ChatViewModel by viewModels()
    private var currentReplyMode = "manual"
    private val chatHistory = mutableListOf<GroqMessage>()
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->

            if (success && selectedImageUri != null) {
                showImageSendReceivePopup(binding.btnSend, selectedImageUri!!)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            uri?.let {
                showImageSendReceivePopup(binding.btnSend, it)
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentChatBinding.bind(view)
        callAlarmManager =
            CallAlarmManager(
                requireContext()
            )
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )

            insets
        }

        isFakeCall = arguments?.getBoolean("isFakeCall", false) ?: false
        isFromVideoCall = arguments?.getBoolean("isFromVideoCall", false) ?: false
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.replyMode.collect {
                    currentReplyMode = it
                }
            }
        }
        parentFragmentManager.setFragmentResultListener(
            "ai_settings",
            viewLifecycleOwner
        ) { _, bundle ->

            currentReplyMode =
                bundle.getString("mode", "manual") ?: "manual"

            viewModel.setReplyMode(currentReplyMode)

            Toast.makeText(
                requireContext(),
                "Selected: $currentReplyMode",
                Toast.LENGTH_SHORT
            ).show()
        }
        isAiChat = arguments?.getBoolean("isAiChat", false) ?: false
        userName = arguments?.getString("name") ?: "User"
        userImage = arguments?.getInt("image") ?: 0
        videoUrl = arguments?.getString("videoUrl") ?: ""
        binding.tvName.text = userName
        chatHistory.clear()

        chatHistory.add(
            GroqMessage(
                role = "system",
                content = """
You are pretending to be $userName.

Character Rules:
- Your name is $userName.
- Reply exactly like $userName would.
- Never say you are an AI.
- Keep replies short (1-2 sentences).
- Use casual chat language.
- Remember previous conversation.
- If the user asks who you are, answer "$userName".
""".trimIndent()
            )
        )
//        if (userImage != 0) {
//            binding.ivUser.setImageResource(userImage)
//        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            if (isFakeCall || isFromVideoCall) {
                findNavController().navigate(
                    R.id.action_chatFragment_to_homeFragment
                )
            } else {
                findNavController().popBackStack()
            }

        }
        messageAdapter = MessageAdapter(messageList)
        binding.rvMessages.adapter = messageAdapter
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
        Glide.with(requireContext())
            .load(videoUrl)
            .centerCrop()
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .thumbnail(0.1f)
            .into(binding.ivUser)

        // Handle keyboard insets - Move content up when keyboard opens

        // Header buttons
        binding.btnBack.setOnClickListener {

            if (isFakeCall || isFromVideoCall) {
                findNavController().navigate(
                    R.id.action_chatFragment_to_homeFragment
                )
            } else {
                findNavController().popBackStack()
            }

        }
        binding.btnAttachment.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.btnCamera.setOnClickListener {

            val file = File(
                requireContext().cacheDir,
                "IMG_${System.currentTimeMillis()}.jpg"
            )

            selectedImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            cameraLauncher.launch(selectedImageUri)
        }
        binding.btnCall.setOnClickListener {

            Log.d("RingingFragment", "Audio Call Clicked")
            Log.d("RingingFragment", "name = $userName")
            Log.d("RingingFragment", "videoUrl = $videoUrl")

            findNavController().navigate(
                R.id.ringingFragment,
                bundleOf(
                    "name" to userName,
                    "videoUrl" to videoUrl,
                    "isVideoCall" to false
                )
            )
        }

        binding.btnVideo.setOnClickListener {

            Log.d("RingingFragment", "Video Call Clicked")
            Log.d("RingingFragment", "name = $userName")
            Log.d("RingingFragment", "videoUrl = $videoUrl")

            findNavController().navigate(
                R.id.ringingFragment,
                bundleOf(
                    "name" to userName,
                    "videoUrl" to videoUrl,
                    "isVideoCall" to true
                )
            )
        }

        binding.btnAi.setOnClickListener {

            AiReplyBottomSheet.newInstance(
                currentReplyMode
            ).show(
                parentFragmentManager,
                "AiReplyBottomSheet"
            )
        }


        // Send button click
        binding.btnSend.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "Mode = $currentReplyMode",
                Toast.LENGTH_SHORT
            ).show()

            val message =
                binding.etMessage.text.toString().trim()

            if (message.isEmpty()) return@setOnClickListener

            if (currentReplyMode == "manual") {

                showSendReceivePopup(
                    binding.btnSend,
                    message
                )

            } else {

                handleSendMessage(message)

                binding.etMessage.text?.clear()


                generateAiReply(message)
            }
        }
    }

    private fun generateAiReply(prompt: String) {

        val loadingPosition = messageList.size

        messageList.add(
            Message(
                text = "",
                isSent = false,
                timestamp = getCurrentTime(),
                messageType = "received_message",
                isLoading = true
            )
        )

        messageAdapter.notifyItemInserted(loadingPosition)
        binding.rvMessages.scrollToPosition(loadingPosition)

        lifecycleScope.launch {

            try {

                // Save user message
                chatHistory.add(
                    GroqMessage(
                        role = "user",
                        content = prompt
                    )
                )

                // Keep system prompt + last 20 messages
                val messagesToSend = listOf(chatHistory.first()) +
                        chatHistory.drop(1).takeLast(20)

                val request = GroqRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = messagesToSend
                )

                val response = GroqClient.api.generateText(request)

                val reply = if (response.isSuccessful) {
                    response.body()
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?: "No response"
                } else {
                    "Error: ${response.code()}"
                }

                // Save assistant reply
                chatHistory.add(
                    GroqMessage(
                        role = "assistant",
                        content = reply
                    )
                )

                messageList[loadingPosition] = Message(
                    text = reply,
                    isSent = false,
                    timestamp = getCurrentTime(),
                    messageType = "received_message"
                )

                messageAdapter.notifyItemChanged(loadingPosition)

            } catch (e: Exception) {

                messageList[loadingPosition] = Message(
                    text = e.message ?: "Unknown error",
                    isSent = false,
                    timestamp = getCurrentTime(),
                    messageType = "received_message"
                )

                messageAdapter.notifyItemChanged(loadingPosition)
            }
        }
    }
    private fun showSendReceivePopup(anchorView: View, message: String) {
        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_send_receive, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false // IMPORTANT
        )

        popupWindow.isFocusable = false
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        popupWindow.showAtLocation(
            anchorView,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] - popupHeight
        )

        // Keep EditText focused
        binding.etMessage.requestFocus()

        popupView.findViewById<View>(R.id.sendOption).setOnClickListener {
            handleSendMessage(message)
            binding.etMessage.text?.clear()

            hideKeyboard() // hide only after selection

            popupWindow.dismiss()
        }

        popupView.findViewById<View>(R.id.receiveOption).setOnClickListener {
            handleReceiveMessage(message)
            binding.etMessage.text?.clear()

            hideKeyboard() // hide only after selection

            popupWindow.dismiss()
        }
    }

    private fun showImageSendReceivePopup(
        anchorView: View,
        uri: Uri
    ) {

        val popupView = LayoutInflater.from(requireContext())
            .inflate(R.layout.popup_send_receive, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )

        popupWindow.isFocusable = false
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val popupHeight = popupView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        popupWindow.showAtLocation(
            anchorView,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] - popupHeight
        )

        popupView.findViewById<View>(R.id.sendOption).setOnClickListener {

            addImageMessage(uri, true)

            hideKeyboard()

            popupWindow.dismiss()
        }

        popupView.findViewById<View>(R.id.receiveOption).setOnClickListener {

            addImageMessage(uri, false)

            hideKeyboard()

            popupWindow.dismiss()
        }
    }

    private fun addImageMessage(
        uri: Uri,
        isSent: Boolean
    ) {

        messageList.add(
            Message(
                imageUri = uri.toString(),
                isSent = isSent,
                timestamp = getCurrentTime(),
                messageType = if (isSent)
                    "sent_image"
                else
                    "received_image"
            )
        )

        messageAdapter.notifyItemInserted(
            messageList.size - 1
        )

        binding.rvMessages.scrollToPosition(
            messageList.size - 1
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        imm.hideSoftInputFromWindow(
            binding.etMessage.windowToken,
            0
        )
    }

    private fun handleSendMessage(message: String) {
        val currentTime = getCurrentTime()
        messageList.add(
            Message(
                text = message,
                isSent = true,
                timestamp = currentTime,
                messageType = "sent_message"
            )
        )
        messageAdapter.notifyItemInserted(messageList.size - 1)
        binding.rvMessages.scrollToPosition(messageList.size - 1)

        Toast.makeText(
            requireContext(),
            "Message Sent",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleReceiveMessage(message: String) {
        val currentTime = getCurrentTime()
        messageList.add(
            Message(
                text = message,
                isSent = false,
                timestamp = currentTime,
                messageType = "received_message"
            )
        )
        messageAdapter.notifyItemInserted(messageList.size - 1)
        binding.rvMessages.scrollToPosition(messageList.size - 1)

        Toast.makeText(
            requireContext(),
            "Message Received",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        return dateFormat.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}