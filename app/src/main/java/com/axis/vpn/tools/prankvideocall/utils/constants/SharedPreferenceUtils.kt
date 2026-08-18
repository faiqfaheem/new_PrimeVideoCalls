package com.axis.vpn.tools.prankvideocall.utils.constants

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceUtils(
    private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }
}