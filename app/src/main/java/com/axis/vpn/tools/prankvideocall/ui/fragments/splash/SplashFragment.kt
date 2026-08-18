package com.axis.vpn.tools.prankvideocall.ui.fragments.splash

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentSplashBinding
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
class SplashFragment :
    BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private var progressAnimator: ValueAnimator? = null

    override fun onViewCreated() {
        if (requireActivity().intent.getBooleanExtra(EXTRA_START_FAKE_CALL, false)) {
            return
        }

        disableBackPress()
        startProgress()
    }


    private fun startProgress() {
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = SPLASH_DURATION
            interpolator = LinearInterpolator()

            addUpdateListener {
                binding.progressBar.progress = it.animatedValue as Int
            }

            doOnEnd {
                navigateScreen()
            }

            start()
        }
    }

    private fun navigateScreen() {
        if (!isAdded) return

        if (diComponent.sharedPreferenceUtils.isFirstLaunch) {
            diComponent.sharedPreferenceUtils.isFirstLaunch = false

            findNavController().navigate(
                R.id.action_splashFragment_to_languageFragment
            )
        } else {
            findNavController().navigate(
                R.id.action_splashFragment_to_homeFragment
            )
        }
    }
    override fun onDestroyView() {
        progressAnimator?.cancel()
        progressAnimator = null
        super.onDestroyView()
    }

    companion object {
        private const val SPLASH_DURATION = 6000L
        private const val EXTRA_START_FAKE_CALL = "startFakeCall"
    }
}