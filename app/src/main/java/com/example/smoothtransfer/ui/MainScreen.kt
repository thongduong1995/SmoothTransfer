package com.example.smoothtransfer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smoothtransfer.ui.navigation.AppNavGraph
import com.example.smoothtransfer.ui.navigation.BottomNavScreen
import com.example.smoothtransfer.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onThemeChange: (Boolean) -> Unit,
    mainViewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isBottomBarVisible by mainViewModel.isBottomBarVisible.collectAsState()
    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigationBar(navController)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AppNavGraph(
                navController = navController,
                mainViewModel = mainViewModel,
                onThemeChange = onThemeChange
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavScreen.PhoneClone,
        BottomNavScreen.FileShare,
        BottomNavScreen.Settings
    )

    NavigationBar(

        // Sử dụng màu nền của theme và thêm một chút "độ cao" (elevation)
        // 3.dp là một giá trị phổ biến cho BottomNavBar
        modifier = Modifier
            // Đặt một chiều cao mới, nhỏ hơn 80.dp. Ví dụ: 64.dp
            .height(84.dp),
        //windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                selected = isSelected,
                icon = { Icon(screen.icon, contentDescription = stringResource(screen.title)) },
                label = { Text(stringResource(screen.title)) },
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2962FF),
                    selectedTextColor = Color(0xFF2962FF),

                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
