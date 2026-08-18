package com.axis.vpn.tools.prankvideocall.ui.fragments.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LanguageViewModel : ViewModel() {

    private val languages = listOf(
        LanguageModel("Arabic", "(العربية)"),
        LanguageModel("English", "(English)"),
        LanguageModel("French", "(Français)"),
        LanguageModel("German", "(Deutsch)"),
        LanguageModel("Portuguese", "(Português)"),
        LanguageModel("Russian", "(Русский)"),
        LanguageModel("Spanish", "(Español)")
    )

    private val _languageList = MutableLiveData(languages)
    val languageList: LiveData<List<LanguageModel>> = _languageList

    private val _selectedLanguage = MutableLiveData<LanguageModel?>()
    val selectedLanguage: LiveData<LanguageModel?> = _selectedLanguage

    fun updateSelected(selected: LanguageModel) {
        val updatedList = _languageList.value?.map {
            it.copy(isSelected = it.title == selected.title)
        }
        _languageList.value = updatedList
        _selectedLanguage.value = selected
    }
}