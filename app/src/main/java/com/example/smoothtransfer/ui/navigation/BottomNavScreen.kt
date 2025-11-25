package com.example.smoothtransfer.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smoothtransfer.R

sealed class BottomNavScreen(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector
) {
    object PhoneClone : BottomNavScreen("phone_clone_main_graph", R.string.phone_clone, Icons.Default.PhoneAndroid)
    object FileShare : BottomNavScreen("file_share_main_graph", R.string.file_share, Icons.Default.Send)
    object Settings : BottomNavScreen("settings_main_graph", R.string.settings, Icons.Default.Settings)
}
