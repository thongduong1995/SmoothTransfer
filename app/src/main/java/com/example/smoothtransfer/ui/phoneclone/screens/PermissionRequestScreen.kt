package com.example.smoothtransfer.ui.phoneclone.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestScreen(
    action: PhoneClone.PhoneCloneActions,
    isSender: Boolean,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var allPermissionsGranted by remember {
        mutableStateOf(PermissionHelper.hasAllPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Re-check all permissions after user response
        allPermissionsGranted = PermissionHelper.hasAllPermissions(context)

        if (allPermissionsGranted) {
            // All permissions granted, proceed to transfer method selection
            action.onEvent(PhoneClone.Event.PermissionsGranted(isSender))
        }
    }

    LaunchedEffect(Unit) {
        // Re-check permissions
        allPermissionsGranted = PermissionHelper.hasAllPermissions(context)

        if (!allPermissionsGranted) {
            val missingPermissions = PermissionHelper.getMissingPermissions(context)
            if (missingPermissions.isNotEmpty()) {
                // Request all missing permissions at once
                permissionLauncher.launch(missingPermissions)
            }
        } else {
            // Already have all permissions, proceed
            action.onEvent(PhoneClone.Event.PermissionsGranted(isSender))
        }
    }

    // Re-check permissions when screen becomes visible again
    LaunchedEffect(allPermissionsGranted) {
        if (!allPermissionsGranted) {
            val currentStatus = PermissionHelper.hasAllPermissions(context)
            if (currentStatus != allPermissionsGranted) {
                allPermissionsGranted = currentStatus
                if (allPermissionsGranted) {
                    action.onEvent(PhoneClone.Event.PermissionsGranted(isSender))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Permissions Required",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        action.onEvent(PhoneClone.Event.BackPressed)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (allPermissionsGranted) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "All permissions granted!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        text = "Permissions Required",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "This app needs the following permissions to transfer data via Wi-Fi:",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // List of required permissions
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "• Camera (for QR code scanning)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Location (for Wi-Fi Aware)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Nearby devices (for Wi-Fi Aware)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val missingPermissions = PermissionHelper.getMissingPermissions(context)
                            if (missingPermissions.isNotEmpty()) {
                                permissionLauncher.launch(missingPermissions)
                            }
                        }
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }
    }
}