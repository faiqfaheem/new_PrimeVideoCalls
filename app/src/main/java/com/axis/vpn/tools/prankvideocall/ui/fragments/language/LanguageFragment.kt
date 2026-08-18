package com.axis.vpn.tools.prankvideocall.ui.fragments.language

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.FragmentLanguageBinding
import com.axis.vpn.tools.prankvideocall.utils.base.fragments.BaseFragment
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
import com.axis.vpn.tools.prankvideocall.utils.extension.navigateTo
import com.axis.vpn.tools.prankvideocall.utils.extension.setEdgeToEdgePadding

import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.apply

class LanguageFragment :
    BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {


    private val viewModel by viewModel<LanguageViewModel>()

    private lateinit var adapter: LanguageAdapter
    var isFromMain: Boolean = false

    private var isInterLoading = false
    private var fromSettings = false

    override fun onViewCreated() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fromSettings = arguments?.getBoolean("from_settings", false) ?: false
        initRecycler()
        observeData()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isFromMain) {
                        findNavController().navigateUp()
                    } else {
                        disableBackPress()
                    }
                }
            })



        binding.mbContinueLanguage.setOnClickListener {

            val selected = viewModel.selectedLanguage.value

            selected?.let {
                val code = getLangCode(it.title)
                saveLanguageToPrefs(it.title, code)
                setLocale(code)
            }

            navigateScreen()
        }
    }


    private fun getLangCode(name: String): String {
        return when (name) {
            "Arabic" -> "ar"
            "English" -> "en"
            "French" -> "fr"
            "German" -> "de"
            "Portuguese" -> "pt"
            "Russian" -> "ru"
            "Spanish" -> "es"
            else -> "en"
        }
    }


    private fun saveLanguageToPrefs(langName: String, langCode: String) {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("selected_language", langCode)
            .putString("selected_language_name", langName)
            .apply()
    }
    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(
            config,
            requireActivity().resources.displayMetrics
        )
    }

    private fun initRecycler() {
        adapter = LanguageAdapter { selected ->
            viewModel.updateSelected(selected)
            binding.mbContinueLanguage.isEnabled = true
            binding.mbContinueLanguage.alpha = 1f
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.languageList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

    }


    private fun navigateScreen() {
        if(fromSettings){
            navigateTo(
                R.id.languageFragment,
                R.id.action_languageFragment_to_settingFragment
            )
        }else{
            navigateTo(
                R.id.languageFragment,
                R.id.action_languageFragment_to_onBoardingFragment
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}