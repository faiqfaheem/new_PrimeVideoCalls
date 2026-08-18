package com.axis.vpn.tools.prankvideocall

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.axis.vpn.tools.prankvideocall.di.DIComponent
import com.axis.vpn.tools.prankvideocall.di.KoinModules
import com.axis.vpn.tools.prankvideocall.utils.constants.LocaleHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.core.lazyModules
class MyApp : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private val diComponent = DIComponent()
    private var currentActivity: Activity? = null
    private var isAppInBackground: Boolean = false
    var isShowingAppOpenAd = false

    private var previousFragmentId: Int? = null

    override fun onCreate() {
        super<Application>.onCreate()

//        val remoteConfig = FirebaseRemoteConfig.getInstance()
//        val configSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(3600)
//            .build()
//        remoteConfig.setConfigSettingsAsync(configSettings)
//        remoteConfig.setDefaultsAsync(
//            mapOf("is_onboarding" to true)
//        )
        LocaleHelper.applySavedLanguage(this)
//        remoteConfig.fetchAndActivate()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d("RemoteConfig", "Config fetched and activated.")
//                } else {
//                    Log.w("RemoteConfig", "Fetch failed.")
//                }
//            }
        initKoin()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        registerActivityLifecycleCallbacks(this)
//        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(this)
    }

    @OptIn(KoinExperimentalAPI::class)
    private fun initKoin() {
        val koinModules = KoinModules()
        GlobalContext.startKoin {
            androidContext(this@MyApp)
            modules(koinModules.modulesList)
            lazyModules(koinModules.backgroundModuleList)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInBackground = true
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        const val TAG = "MyApplications"
    }
}