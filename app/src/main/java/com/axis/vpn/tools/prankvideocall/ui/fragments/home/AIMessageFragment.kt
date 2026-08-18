package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.databinding.FragmentAIMessageBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.ChatUserAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankChatResponse
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue
class AIMessageFragment : Fragment(R.layout.fragment_a_i_message) {

    private var _binding: FragmentAIMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatUserAdapter

    private val homeDataViewModel: HomeDataViewModel by activityViewModel()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAIMessageBinding.bind(view)

        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {

        chatAdapter = ChatUserAdapter { item ->
            openChat(item)
        }

        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        homeDataViewModel.chat.observe(viewLifecycleOwner) { list ->
            chatAdapter.submitList(list)
        }
    }

    private fun openChat(item: PrankChatResponse) {
        Log.d("OpenChat", "Video URL: ${item.videoUrl}")
        findNavController().navigate(
            R.id.chatFragment,
            bundleOf(
                "name" to item.name,
                "videoUrl" to item.videoUrl,
                "isAiChat" to true
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}