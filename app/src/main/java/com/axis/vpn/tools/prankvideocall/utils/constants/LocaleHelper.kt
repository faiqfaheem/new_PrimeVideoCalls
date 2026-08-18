package com.axis.vpn.tools.prankvideocall.utils.constants

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANG_CODE = "selected_language"
    private const val KEY_LANG_NAME = "selected_language_name"

    fun saveLanguage(context: Context, langName: String, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANG_CODE, langCode)
            .putString(KEY_LANG_NAME, langName)
            .apply()
    }

    fun getSavedLanguageCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG_CODE, "en") ?: "en"
    }

    fun getSavedLanguageName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG_NAME, "English") ?: "English"
    }

    fun wrap(context: Context): Context {
        val langCode = getSavedLanguageCode(context)
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun applySavedLanguage(context: Context) {
        val langCode = getSavedLanguageCode(context)
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}