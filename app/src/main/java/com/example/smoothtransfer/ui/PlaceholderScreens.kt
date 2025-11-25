package com.example.smoothtransfer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

// --- Màn hình dùng chung ---
@Composable fun ConnectingScreen() { Text("Connecting...") }
@Composable fun ConnectedScreen() { Text("Connected!") }
@Composable fun TransferringScreen() { Text("Transferring...") }
@Composable fun SelectTransferMethodScreen() { Text("Select Transfer Method") }
@Composable fun CloneQrScreen() { Text("Clone QR Screen") }
@Composable fun ContentListScreen() { Text("Content List Screen") }
@Composable fun RestoringScreen() { Text("Restoring Data...") }
@Composable fun TransferCompleteScreen() { Text("Transfer Complete!") }

// --- File Share ---
@Composable
fun FileShareHomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { navController.navigate("fileshare_contentlist") }) {
            Text("Start File Share")
        }
    }
}
@Composable fun ShareSelectContentScreen() { Text("Share: Select Content") }
@Composable fun FileShareQrScreen() { Text("File Share QR Screen") }
@Composable fun ShareCompleteScreen() { Text("Share Complete!") }

// --- Settings ---
@Composable
fun SettingsHomeScreen(navController: NavController) {
    Text("Settings Home")
}
@Composable fun LanguageScreen() { Text("Language Settings") }
@Composable fun DarkModeScreen() { Text("Theme Settings") }
@Composable fun TransferHistoryScreen() { Text("Transfer History") }
