package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentHomeBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.HomePagerAdapter
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.constants.PermissionHelper
import com.axis.vpn.tools.prankvideocall.utils.extension.showPermissionSettingsDialog
class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
) {
    private var cameraDeniedCount = 0
    private var isNavigatingFromViewPager = false // Track source of navigation

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted: Boolean ->
            if (granted) {
                cameraDeniedCount = 0
                checkAlarmPermission()
            } else {
                handleCameraDenied()
            }
        }

    private fun handleCameraDenied() {
        cameraDeniedCount++

        if (cameraDeniedCount == 1) {
            requestCameraPermission()
        } else {
            showPermissionSettingsDialog(
                message = "Camera permission is required to use this feature. Please enable it from Settings."
            )
        }
    }

    private fun requestCameraPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun onViewCreated() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkAllPermissions()

        setupViewPager()
        setupBottomNavigation()
    }

    private fun checkAllPermissions() {
        if (PermissionHelper.hasCameraPermission(requireContext())) {
            checkAlarmPermission()
        } else {
            requestCameraPermission()
        }
    }

    private fun checkAlarmPermission() {
        if (!PermissionHelper.canScheduleExactAlarm(requireContext())) {
            PermissionHelper.openExactAlarmSettings(requireContext())
        }
    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = HomePagerAdapter(this@HomeFragment)
            isUserInputEnabled = true

            registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        // Only update nav if change came from ViewPager, not from nav click
                        isNavigatingFromViewPager = true

                        binding.bottomNavigation.selectedItemId =
                            when (position) {
                                0 -> R.id.nav_start
                                1 -> R.id.nav_ai
                                2 -> R.id.nav_prank
                                3 -> R.id.nav_schedule
                                else -> R.id.nav_start
                            }

                        isNavigatingFromViewPager = false
                    }
                }
            )
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Skip if the selection came from ViewPager page change
            if (isNavigatingFromViewPager) {
                return@setOnItemSelectedListener true
            }

            val page = getPositionFromNavItemId(item.itemId)

            // Only navigate if it's actually a different page
            if (binding.viewPager.currentItem != page) {
                binding.viewPager.setCurrentItem(page, true)
            }

            true
        }
    }

    private fun getPositionFromNavItemId(itemId: Int): Int {
        return when (itemId) {
            R.id.nav_start -> 0
            R.id.nav_ai -> 1
            R.id.nav_prank -> 2
            R.id.nav_schedule -> 3
            else -> 0
        }
    }

    fun openScheduleTab(
        heroName: String,
        videoUrl: String,
        delay: Int,
        isCustom: Boolean,
        time: String
    ) {

        // Send schedule information
        childFragmentManager.setFragmentResult(
            "schedule_data",
            bundleOf(
                "name" to heroName,
                "videoUrl" to videoUrl,
                "delay" to delay,
                "isCustom" to isCustom,
                "time" to time
            )
        )

        // Immediately show Schedule tab
        binding.viewPager.setCurrentItem(
            3,
            false
        )
    }

    override fun onResume() {
        super.onResume()
    }
}