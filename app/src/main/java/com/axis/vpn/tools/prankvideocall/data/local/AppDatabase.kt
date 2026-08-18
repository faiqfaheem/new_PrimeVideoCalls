package com.axis.vpn.tools.prankvideocall.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity

@Database(
    entities = [
        CustomCallerEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){


    abstract fun customCallerDao(): CustomCallerDao


}