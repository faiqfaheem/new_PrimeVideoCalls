package com.axis.vpn.tools.prankvideocall.data.remote

import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.GroqMessage

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>
)
data class Message(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)