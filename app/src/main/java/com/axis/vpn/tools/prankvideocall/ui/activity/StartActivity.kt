package com.axis.vpn.tools.prankvideocall.ui.activity

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.databinding.ActivityStartBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.HomeFragment
import com.axis.vpn.tools.prankvideocall.utils.base.activity.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartActivity : BaseActivity<ActivityStartBinding>(ActivityStartBinding::inflate) {
    private val homeDataViewModel: HomeDataViewModel by viewModel()

    override fun onPreCreated() {
        super.onPreCreated()
        // ⭐ CRITICAL: Disable window padding for full-screen video
        // This prevents the white theme background from showing

    }

    override fun onCreated() {
        // Load APIs only once
        homeDataViewModel.preloadData(this)

        setupBackPress()

        if (intent.getBooleanExtra("startFakeCall", false)) {

            val navHostFragment =
                supportFragmentManager.findFragmentById(
                    R.id.fragmentContainerView
                ) as NavHostFragment

            val navController =
                navHostFragment.navController

            val graph =
                navController.navInflater.inflate(
                    R.navigation.nav_graph
                )

            graph.setStartDestination(
                R.id.homeFragment
            )

            navController.graph = graph

            binding.root.post {
                handleFakeCallIntent(intent)
            }

        } else {

            binding.root.post {
                handleFakeCallIntent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent?.let {
            setIntent(it)

            binding.root.post {
                handleFakeCallIntent(it)
            }
        }
    }

    private fun setupBackPress() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    handleHomeBackPress()

                }
            }
        )
    }

    private fun handleHomeBackPress() {

        Log.d("BACK_PRESS", "handleHomeBackPress called")

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.fragmentContainerView
            ) as NavHostFragment

        val navController = navHostFragment.navController

        Log.d(
            "BACK_PRESS",
            "Current Destination = ${navController.currentDestination?.label}"
        )

        val fragment =
            navHostFragment.childFragmentManager.primaryNavigationFragment

        Log.d(
            "BACK_PRESS",
            "Current Fragment = ${fragment?.javaClass?.simpleName}"
        )


        if (fragment is HomeFragment) {

            Log.d("BACK_PRESS", "HomeFragment Found")

            Log.d(
                "BACK_PRESS",
                "Current Tab = ${fragment.binding.viewPager.currentItem}"
            )

            if (fragment.binding.viewPager.currentItem != 0) {

                Log.d("BACK_PRESS", "Moving to first tab")

                fragment.binding.viewPager.currentItem = 0

            } else {

                Log.d("BACK_PRESS", "Showing Exit Dialog")

                showExitDialog()

            }

        } else {

            Log.d("BACK_PRESS", "Popping Back Stack")

            if (!navController.popBackStack()) {

                Log.d("BACK_PRESS", "Finishing Activity")

                finishAffinity()

            }
        }
    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.exit_dialogue)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val rateUs = dialog.findViewById<Button>(R.id.RateUs)
        val exitApp = dialog.findViewById<Button>(R.id.ExitBtn)
        rateUs.setOnClickListener {
            rateApp(this)
            dialog.dismiss()
        }
        exitApp.setOnClickListener {
            finish()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun rateApp(context: Context) {
        val appPackageName = context.packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open Play Store", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFakeCallIntent(intent: Intent) {

        if (!intent.getBooleanExtra("startFakeCall", false)) {
            return
        }

        val delay = intent.getLongExtra("delay", 0L)
        val heroName = intent.getStringExtra("name") ?: ""
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val audioUrl = intent.getStringExtra("audioUrl") ?: ""
        val heroImage = intent.getIntExtra("image", 0)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.fragmentContainerView
            ) as NavHostFragment

        val navController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setPopUpTo(
                navController.graph.startDestinationId,
                true
            )
            .build()
        navController.navigate(
            R.id.fakeIncomingCallFragment,
            bundleOf(
                "delay" to delay,
                "name" to heroName,
                "videoUrl" to videoUrl,
                "audioUrl" to audioUrl
            )
        )

    }
}