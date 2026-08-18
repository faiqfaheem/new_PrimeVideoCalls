package com.axis.vpn.tools.prankvideocall.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_callers")
data class CustomCallerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val imagePath: String,

    val videoUri: String,

    val audioUri: String
)