package com.example.smoothtransfer.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.smoothtransfer.ui.ConnectedScreen
import com.example.smoothtransfer.ui.ConnectingScreen
import com.example.smoothtransfer.ui.DarkModeScreen
import com.example.smoothtransfer.ui.FileShareHomeScreen
import com.example.smoothtransfer.ui.FileShareQrScreen
import com.example.smoothtransfer.ui.LanguageScreen
import com.example.smoothtransfer.viewmodel.MainViewModel
import com.example.smoothtransfer.ui.ShareCompleteScreen
import com.example.smoothtransfer.ui.ShareSelectContentScreen
import com.example.smoothtransfer.ui.TransferHistoryScreen
import com.example.smoothtransfer.ui.TransferringScreen
import com.example.smoothtransfer.ui.phoneclone.PhoneCloneFlowHostScreen
import com.example.smoothtransfer.ui.settings.SettingsScreen

fun NavGraphBuilder.phoneCloneNavGraph(
    navController: NavController,
    mainViewModel: MainViewModel // <<< Nhận vào MainViewModel
) {
    composable(route = BottomNavScreen.PhoneClone.route) {
        PhoneCloneFlowHostScreen(
            navController = navController,
            mainViewModel = mainViewModel
        )
    }
}

fun NavGraphBuilder.fileShareNavGraph(navController: NavController) {
    navigation(
        startDestination = "fileshare_home",
        route = BottomNavScreen.FileShare.route
    ) {
        composable("fileshare_home") { FileShareHomeScreen(navController) }
        composable("fileshare_contentlist") { ShareSelectContentScreen() }
        composable("fileshare_qr") { FileShareQrScreen() }
        // Tái sử dụng các màn hình chung
        composable("fileshare_connecting") { ConnectingScreen() }
        composable("fileshare_connected") { ConnectedScreen() }
        composable("fileshare_transferring") { TransferringScreen() }
        composable("fileshare_complete") { ShareCompleteScreen() }
    }
}

fun NavGraphBuilder.settingsNavGraph(navController: NavController, onThemeChange: (Boolean) -> Unit) {
    navigation(
        startDestination = "settings_home",
        route = BottomNavScreen.Settings.route
    ) {
        composable("settings_home") {
            SettingsScreen(
                onThemeChange = onThemeChange
            )
        }
        composable("settings_language") { LanguageScreen() }
        composable("settings_theme") { DarkModeScreen() }
        composable("settings_history") { TransferHistoryScreen() }
    }
}
