package com.axis.vpn.tools.prankvideocall.ui.fragments.onboarding

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context
) :
    FragmentStateAdapter(fragmentActivity) {

    @SuppressLint("ResourceType")
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> IntroFragmentOne()
            1 -> IntroFragmentTwo()
            2 -> IntroFragmentThree()
            3 -> IntroFragmentFour()
            else -> IntroFragmentOne()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}