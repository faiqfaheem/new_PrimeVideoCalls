package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class Sound(

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("soundUrl")
    val soundUrl: String,

    @SerializedName("thumbnail")
    val thumbnail: String
)