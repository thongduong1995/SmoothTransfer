package com.example.smoothtransfer.data.local


import android.content.Context
import android.content.res.Configuration
import android.os.Build

import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Supported languages
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        VIETNAMESE("vi", "Tiếng Việt"),
        KOREAN("ko", "한국어"),
        JAPANESE("ja", "日本語");

        companion object {
            fun fromCode(code: String): Language {
                return values().find { it.code == code } ?: ENGLISH
            }
        }
    }

    /**
     * Get current language from SharedPreferences
     */
    fun getCurrentLanguage(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code
        return Language.fromCode(languageCode)
    }

    /**
     * Save selected language to SharedPreferences
     */
    fun saveLanguage(context: Context, language: Language) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    /**
     * Set app locale based on selected language
     */
    fun setAppLocale(context: Context, language: Language): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        return context
    }

    /**
     * Update app locale and recreate activity
     */
    fun updateAppLocale(context: Context, language: Language) {
        saveLanguage(context, language)
        setAppLocale(context, language)

        // Recreate activity to apply language change
        if (context is android.app.Activity) {
            context.recreate()
        }
    }
}

