package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
class ChatViewModel : ViewModel() {

    private val _replyMode =
        MutableStateFlow("manual")

    val replyMode =
        _replyMode.asStateFlow()

    fun setReplyMode(
        mode: String
    ) {
        _replyMode.value = mode
    }
}