package com.example.smoothtransfer

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.smoothtransfer.data.local.LanguageManager
import com.example.smoothtransfer.data.local.ThemeManager
import com.example.smoothtransfer.ui.MainScreen
import com.example.smoothtransfer.ui.navigation.DeepLinkHandler
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.let { LanguageManager.setAppLocale(it, LanguageManager.getCurrentLanguage(it)) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)
        enableEdgeToEdge()

        val themeManager = ThemeManager(this)
        val initialDarkMode = themeManager.isDarkMode()

        setContent {
            var isDarkMode  by remember { mutableStateOf(initialDarkMode) }

            SmoothTransferTheme (
                darkTheme = isDarkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Gọi thẳng vào MainScreen
                    MainScreen(
                        onThemeChange = {
                            isDarkMode = it
                            themeManager.setDarkMode(it)
                        }
                    )
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {
            lifecycleScope.launch {
                // >>> THAY ĐỔI QUAN TRỌNG Ở ĐÂY <<<
                // Gửi một Event cụ thể, yêu cầu chuyển sang luồng Cable
                DeepLinkHandler.postEvent(PhoneClone.Event.UsbAttached(true))
            }
        }
    }
}
