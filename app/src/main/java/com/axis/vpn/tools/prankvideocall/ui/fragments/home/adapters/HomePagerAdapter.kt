package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.AIMessageFragment
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.PrankSoundFragment
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.ScheduleCallFragment
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.StartFragment

class HomePagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private val fragmentCache = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return fragmentCache.getOrPut(position) {
            when (position) {
                0 -> StartFragment()
                1 -> AIMessageFragment()
                2 -> PrankSoundFragment()
                3 -> ScheduleCallFragment()
                else -> StartFragment()
            }
        }
    }

    fun clearCache() {
        fragmentCache.clear()
    }
}
