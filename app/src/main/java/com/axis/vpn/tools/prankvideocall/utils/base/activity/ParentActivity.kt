package com.axis.vpn.tools.prankvideocall.utils.base.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.utils.constants.Constants.TAG
import com.axis.vpn.tools.prankvideocall.utils.constants.NetworkMonitor
import com.axis.vpn.tools.prankvideocall.utils.constants.NoInternetDialog
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch

abstract class ParentActivity<T : ViewBinding>(private val bindingFactory: (LayoutInflater) -> T) : AppCompatActivity() {

    protected val binding by lazy { bindingFactory(layoutInflater) }

    protected var includeTopPadding = true
    protected var includeBottomPadding = true
    protected var enableKeyboardInsets = false
    protected var isLightStatusBars = true
    private lateinit var networkMonitor: NetworkMonitor

    private var noInternetDialog: NoInternetDialog? = null
    var statusBarHeight = 0
    var navigationBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        onPreCreated()

        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        observeNetwork()

        onCreated()

        initObservers()
    }
    private fun observeNetwork() {

        networkMonitor = NetworkMonitor(this)

        networkMonitor.register()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                networkMonitor.isConnected.collect { isConnected ->

                    if (isConnected) {
                        hideNoInternetDialog()
                    } else {
                        showNoInternetDialog()
                    }
                }
            }
        }
    }
    private fun showNoInternetDialog() {

        if (noInternetDialog?.isShowing == true)
            return

        noInternetDialog = NoInternetDialog(this)

        noInternetDialog?.show()
    }

    private fun hideNoInternetDialog() {

        noInternetDialog?.dismiss()

        noInternetDialog = null
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(TAG, "onNewIntent: Called with intent")

        // Update the activity's intent
        setIntent(intent)

    }

    /**
     * Navigate to fake incoming call if intent has the flag
     */

    open fun onPreCreated() {
        enableKeyboardInsets = true
        includeBottomPadding = true
    }
    open fun initObservers() {}

//    private fun setPadding() {
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
//            WindowCompat.getInsetsController(window, window.decorView).apply {
//                isAppearanceLightStatusBars = isLightStatusBars
//            }
//
//            when (enableKeyboardInsets) {
//                true -> setPaddingKeyboard(v, insets)
//                false -> setPaddingNormal(v, insets)
//            }
//            WindowInsetsCompat.CONSUMED
//        }
//    }

//    private fun setPaddingNormal(v: View, insets: WindowInsetsCompat) {
//        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
//
//        if (bars.top != 0) {
//            statusBarHeight = bars.top
//        }
//        if (bars.bottom != 0) {
//            navigationBarHeight = bars.bottom
//        }
//
//        val topPadding = if (includeTopPadding) bars.top else 0
//        val bottomPadding = if (includeBottomPadding) bars.bottom else 0
//
//        v.updatePadding(
//            left = bars.left,
//            top = topPadding,
//            right = bars.right,
//            bottom = bottomPadding
//        )
//    }
//
//    private fun setPaddingKeyboard(v: View, insets: WindowInsetsCompat) {
//        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
//        val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
//
//        if (systemBars.top != 0) {
//            statusBarHeight = systemBars.top
//        }
//
//        if (systemBars.bottom != 0) {
//            navigationBarHeight = systemBars.bottom
//        }
//
//        // Use the maximum of nav bar or IME for bottom padding
//        val topPadding = if (includeTopPadding) maxOf(systemBars.top, cutout.top, statusBarHeight) else 0
//        val bottomPadding = if (includeBottomPadding) maxOf(systemBars.bottom, imeInsets.bottom) else 0
//
//        v.updatePadding(
//            left = systemBars.left,
//            top = topPadding,
//            right = systemBars.right,
//            bottom = bottomPadding
//        )
//    }

    protected open fun installSplashTheme() {
        Log.d(TAG, "installSplashTheme: installed")
        //installSplashScreen()
    }

    protected open fun enableMaterialDynamicTheme() {
        Log.d(TAG, "enableMaterialDynamicTheme: enabling")
        DynamicColors.applyToActivityIfAvailable(this)
    }

    /**
     * @param type
     *     0: Show SystemBars
     *     1: Hide SystemBars
     *     2: Hide StatusBars
     *     3: Hide NavigationBars
     */

    protected open fun hideStatusBar(type: Int) {
        Log.d(TAG, "hideStatusBar: Showing/Hiding: Type: $type")
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = when (type) {
                0 -> WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                else -> WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            when (type) {
                0 -> show(WindowInsetsCompat.Type.systemBars())
                1 -> show(WindowInsetsCompat.Type.systemBars())
                2 -> show(WindowInsetsCompat.Type.statusBars())
                3 -> show(WindowInsetsCompat.Type.navigationBars())
                4 -> {
                    hide(WindowInsetsCompat.Type.navigationBars())

                }
                5 -> show(WindowInsetsCompat.Type.navigationBars())
                6 -> show(WindowInsetsCompat.Type.navigationBars())
                else -> show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        networkMonitor.unregister()
    }
    abstract fun onCreated()
}