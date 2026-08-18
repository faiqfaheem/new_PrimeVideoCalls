package com.axis.vpn.tools.prankvideocall.utils.base.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class ParentFragment<T : ViewBinding>(val bindingFactory: (LayoutInflater) -> T) : Fragment() {


    private var _binding: T? = null
    val binding get() = _binding ?: throw IllegalStateException("Accessing binding outside of view lifecycle")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = bindingFactory(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated(savedInstanceState)
        onViewCreated()
        initObservers()
    }

    /**
     *  @since : Start code...
     */
    open fun onViewCreated(savedInstanceState: Bundle?) {}

    abstract fun onViewCreated()

    open fun initObservers() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}