package com.example.smoothtransfer.ui.phoneclone.screens

// Thêm import cho icon Android
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTransferMethodScreen(
    action: PhoneClone.PhoneCloneActions,
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Phone Clone", fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        action.onEvent(PhoneClone.Event.BackPressed)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Background Animation
            //LottieBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Tiêu đề
                Text(
                    text = "Phone Clone",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                )
                Text(
                    text = "Select methods to transfer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                // 3. Các nút chọn vai trò
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RoleButton(
                        modifier = Modifier.weight(1f),
                        title = "Cable",
                        subtitle = "from this phone",
                        icon = { Icons.Default.PhoneAndroid },
                        backgroundColor = Color(0xFF00C853), // Màu xanh lá cây
                        onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = true)) }
                    )
                    RoleButton(
                        modifier = Modifier.weight(1f),
                        title = "Wifi",
                        subtitle = "on this phone",
                        icon = { Icons.Default.PhoneAndroid }, // Icon nhận
                        backgroundColor = Color(0xFF2962FF), // Màu xanh dương
                        onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = false)) }
                    )
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}




// --- Preview để xem trước giao diện ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SelectTransferMethodScreenPreview() {
    SmoothTransferTheme {
        val fakeAction = object : PhoneClone.PhoneCloneActions {
            override fun onEvent(event: PhoneClone.Event) {
                // Xử lý sự kiện ở đây
            }
        }
        SelectTransferMethodScreen (action = fakeAction, onBackClicked = {})
    }
}
