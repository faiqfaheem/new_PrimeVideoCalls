package com.axis.vpn.tools.prankvideocall.di

import androidx.lifecycle.LiveData
import androidx.navigation.NavDirections
import com.axis.vpn.tools.prankvideocall.utils.constants.SingleLiveEvent

class GeneralObserver {

    val _navDashboardLiveData = SingleLiveEvent<Int>()
    val navDashboardLiveData: LiveData<Int> get() = _navDashboardLiveData

    val _navDashboardDirectionLiveData = SingleLiveEvent<NavDirections>()
    val navDashboardDirectionLiveData: LiveData<NavDirections> get() = _navDashboardDirectionLiveData

}