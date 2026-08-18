package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model

import android.net.Uri
data class Message(
    val text: String = "",
    val imageUri: String? = null,
    val isSent: Boolean,
    val timestamp: String,
    val messageType: String,
    val isLoading: Boolean = false,
    var showActions: Boolean = false
)
data class GroqMessage(
    val role: String,
    val content: String
)
