package com.axis.vpn.tools.prankvideocall.ui.fragments.home.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("sounds")
    val sounds: List<Sound>,

    var isSelected: Boolean = false

)