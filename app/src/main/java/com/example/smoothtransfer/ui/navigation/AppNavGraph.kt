package com.example.smoothtransfer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.smoothtransfer.viewmodel.MainViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onThemeChange: (Boolean) -> Unit
) {
    // startDestination là route của graph mặc định khi mở app
    NavHost(
        navController = navController,
        startDestination = BottomNavScreen.PhoneClone.route
    ) {
        // Mỗi dòng này sẽ định nghĩa một "flow" lồng nhau
        phoneCloneNavGraph(navController, mainViewModel)
        fileShareNavGraph(navController)
        settingsNavGraph(navController, onThemeChange)
    }
}
