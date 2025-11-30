package com.example.smoothtransfer.ui.phoneclone.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.transfer.ServiceType
import com.example.smoothtransfer.transfer.TransferSession
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTransferMethodScreen(
    action: PhoneClone.PhoneCloneActions,
    isSender: Boolean,
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
                        "Phone Clone", fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        action.onEvent(PhoneClone.Event.BackPressed)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
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
                MethodSelectButton(
                    modifier = Modifier.weight(1f),
                    title = "Cable",
                    subtitle = if (isSender) "Send from this phone" else "Receive from this phone",
                    icon = { Icons.Default.PhoneAndroid },
                    backgroundColor = Color(0xFFA3C800), // Màu xanh lá cây
                    onClick = { /*action.onEvent(PhoneClone.Event.RoleSelected(isSender)) */}
                )

                Spacer(modifier = Modifier.weight(1f))

                // 3. Các nút chọn vai trò
                MethodSelectButton(
                    modifier = Modifier.weight(1f),
                    title = "Wifi Aware",
                    subtitle = if (isSender) "Send from this phone without internet" else "Receive from this phone without internet",
                    icon = { Icons.Default.PhoneAndroid },
                    backgroundColor = Color(0xFF00C853), // Màu xanh lá cây
                    onClick = { action.onEvent(PhoneClone.Event.MethodSelected(ServiceType.WIFI_AWARE)) }
                )
                Spacer(modifier = Modifier.weight(1f))

                MethodSelectButton(
                    modifier = Modifier.weight(1f),
                    title = "Wifi",
                    subtitle = if (isSender) "Send from this phone" else "Receive from this phone",
                    icon = { Icons.Default.PhoneAndroid }, // Icon nhận
                    backgroundColor = Color(0xFF2962FF), // Màu xanh dương
                    onClick = { action.onEvent(PhoneClone.Event.MethodSelected(ServiceType.WIFI_AWARE)) }
                )
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun MethodSelectButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    // Thêm hiệu ứng nhấp nháy nhẹ khi nhấn
    val infiniteTransition = rememberInfiniteTransition(label = "role_button_transition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = "role_button_scale"
    )

    Box(
        modifier = modifier
            /* .graphicsLayer {
                 scaleX = scale
                 scaleY = scale
             }*/
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
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
        SelectTransferMethodScreen (action = fakeAction, false, onBackClicked = {})
    }
}
