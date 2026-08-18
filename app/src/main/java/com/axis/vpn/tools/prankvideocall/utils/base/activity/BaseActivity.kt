package com.axis.vpn.tools.prankvideocall.utils.base.activity

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.axis.vpn.tools.prankvideocall.di.DIComponent

abstract class BaseActivity<T : ViewBinding>(bindingFactory: (LayoutInflater) -> T) : ParentActivity<T>(bindingFactory) {
    protected val diComponent by lazy { DIComponent() }

}