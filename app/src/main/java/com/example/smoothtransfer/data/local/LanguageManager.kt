package com.example.smoothtransfer.data.local

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.core.content.edit
import com.example.smoothtransfer.R
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Supported languages with flags
    enum class Language(val code: String, val displayName: String, @DrawableRes val flagResId: Int) {
        ENGLISH("en", "English", R.drawable.ic_flag_us),
        VIETNAMESE("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        JAPAN("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        CHINA("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        US("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        SPAIN("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        SPAIN1("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        SPAIN2("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        SPAIN3("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        GERMANY("vi", "Tiếng Việt", R.drawable.ic_flag_vi),
        KOREAN("ko", "한국어", R.drawable.ic_flag_ko);

        companion object {
            fun fromCode(code: String): Language {
                return entries.find { it.code == code } ?: ENGLISH
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
        prefs.edit { putString(KEY_LANGUAGE, language.code) }
    }

    /**
     * Set app locale based on selected language
     */
    fun setAppLocale(context: Context, language: Language): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
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