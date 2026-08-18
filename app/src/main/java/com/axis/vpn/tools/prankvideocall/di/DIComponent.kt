package com.axis.vpn.tools.prankvideocall.di

import com.axis.vpn.tools.prankvideocall.utils.constants.SharedPreferenceUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class DIComponent : KoinComponent {

    // Utils
    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()
//    val sharedPreferenceUtils by inject<SharedPreferenceUtils>()
//
//    // Managers
//    val internetManager by inject<InternetManagerBlue>()
//
//    // Remote Configuration
//    val remoteConfiguration by inject<RemoteConfiguration>()
//
//    // Admob
//    val appOpenAdManager by inject<AppOpenAdManager>()
//    val appOpenAdsConfig by inject<AppOpenAdsConfig>()
//
//    val interstitialAdsConfig by inject<InterstitialAdsConfig>()
}