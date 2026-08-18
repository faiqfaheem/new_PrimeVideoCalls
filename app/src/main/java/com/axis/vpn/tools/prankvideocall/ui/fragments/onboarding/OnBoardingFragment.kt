package com.axis.vpn.tools.prankvideocall.ui.fragments.onboarding

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentOnBoardingBinding
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.extension.navigateTo



class OnBoardingFragment :
    BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    override fun onViewCreated() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViewPager()
        setupButtons()
        disableBackPress()
    }



    // ---------------- VIEWPAGER ----------------


    private fun setupViewPager() {
        val activity = requireActivity()

        binding.viewPager.adapter =
            IntroViewPagerAdapter(activity, activity)

        binding.pageIndicator.attachTo(binding.viewPager)

        updateButtonState(0) // Initial text

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateButtonState(position)
                }
            }
        )
    }

    // ---------------- BUTTON STATE ----------------

    private fun updateButtonState(position: Int) {

        binding.btnNextStep.text =
            if (position == LAST_PAGE_INDEX) {
                getString(R.string.get_started)
            } else {
                getString(R.string.next)
            }
    }

    // ---------------- BUTTON CLICK ----------------

    private fun setupButtons() {

        binding.btnNextStep.setOnClickListener {
            val current = binding.viewPager.currentItem

            if (current == LAST_PAGE_INDEX) {
                completeOnboarding()
            } else {
                binding.viewPager.setCurrentItem(current + 1, true)
            }
        }
    }


    // ---------------- COMPLETE ONBOARDING ----------------

    private fun completeOnboarding() {
        saveOnboardingCompleted()
        goToMainActivity()
    }


    private fun saveOnboardingCompleted() {
        requireContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .apply()
    }

    private fun goToMainActivity() {
        navigateTo(
            R.id.onBoardingFragment,
            R.id.action_onBoardingFragment_to_homeFragment
        )
    }

    // ---------------- DISABLE BACK ----------------

    private fun disableBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Disabled during onboarding
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private const val PREF_NAME = "app_prefs"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
        private const val LAST_PAGE_INDEX = 3
    }
}
