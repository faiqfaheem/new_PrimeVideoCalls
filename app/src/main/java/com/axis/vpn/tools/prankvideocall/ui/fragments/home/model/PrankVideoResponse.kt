package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model

data class PrankVideoResponse(

    val id: Int,

    val name: String,

    val videoUrl: String

)

data class PrankVideoResponseSchedule(
    val id: Int,
    val name: String,
    val videoUrl: String,
    val audioUrl: String?,   // nullable
    val seconds: Int
)

data class PrankChatResponse(
    val id: Int,
    val name: String,
    val seconds: Int,
    val size: String,
    val videoUrl: String,
    val description: String,
    val messageNo: Int
)