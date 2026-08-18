package com.axis.vpn.tools.prankvideocall.utils.base.fragments

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.axis.vpn.tools.prankvideocall.di.DIComponent

abstract class BaseFragment<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentFragment<T>(bindingFactory) {

    protected val diComponent by lazy { DIComponent() }
}