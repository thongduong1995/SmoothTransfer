package com.example.smoothtransfer.data.local

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )
    
    private val KEY_DARK_MODE = "dark_mode"
    
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }
}

