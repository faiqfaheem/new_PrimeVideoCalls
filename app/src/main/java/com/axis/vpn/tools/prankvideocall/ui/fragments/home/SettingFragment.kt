package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.BuildConfig
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentSettingBinding
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress


class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {


    override fun onViewCreated() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )

            insets
        }
        binding.tvVersion.text = BuildConfig.VERSION_NAME
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.homeFragment)
                }
            })

        initClicks()
    }

    private fun initClicks() {

        // Back
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        // Privacy Policy
        binding.cardPrivacy.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://yourwebsite.com/privacy-policy")
            )
            startActivity(intent)
        }

        // Language
        binding.cardLanguage.setOnClickListener {
            findNavController().navigate(
                R.id.languageFragment,
                bundleOf(
                    "from_settings" to true
                )
            )
        }

        // Share App
        binding.cardShare.setOnClickListener {

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Download ${getString(R.string.app_name)}\n\nhttps://play.google.com/store/apps/details?id=${requireContext().packageName}"
                )
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        // Rate Us
        binding.cardRate.setOnClickListener {
            val uri =
                Uri.parse("market://details?id=${requireContext().packageName}")

            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}