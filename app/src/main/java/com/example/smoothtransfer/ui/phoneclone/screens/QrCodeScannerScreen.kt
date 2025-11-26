package com.example.smoothtransfer.ui.phoneclone.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smoothtransfer.R
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScannerScreen(
    action: PhoneClone.PhoneCloneActions,
    onBackClicked: () -> Unit
) {

    BackHandler {
        action.onEvent(PhoneClone.Event.BackPressed)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.scan_qr_code),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = stringResource(id = R.string.sender_opening_camera),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Secondary instruction: What to scan
            Text(
                text = stringResource(id = R.string.scan_qr_on_receiver),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)  // 85% width for margins
                    .aspectRatio(1f)  // Square shape
                    .clip(RoundedCornerShape(16.dp))  // Rounded corners
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,  // Primary color border
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                QRCodeScanner(
                    onQRCodeScanned = {
                        action.onEvent(PhoneClone.Event.QrCodeScanned(it))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Additional instruction text: Guidance for user
            Text(
                text = stringResource(id = R.string.place_qr_in_frame),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QrCodeScannerScreenPreview() {
    SmoothTransferTheme {
        // Mock actions for preview
        val fakeAction = object : PhoneClone.PhoneCloneActions {
            override fun onEvent(event: PhoneClone.Event) {}
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.scan_qr_code),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    text = stringResource(id = R.string.sender_opening_camera),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.scan_qr_on_receiver),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // Placeholder for the camera view in preview
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.place_qr_in_frame),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
