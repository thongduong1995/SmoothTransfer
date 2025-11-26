package com.example.smoothtransfer.ui.phoneclone.screens

// Thêm import cho icon Android
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.R
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCloneSelectRole(
    action: PhoneClone.PhoneCloneActions,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                text = stringResource(R.string.phone_clone),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.choose_device_role),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))
            PhoneTransferAnimation()
            Spacer(modifier = Modifier.weight(1f))

            // 3. Các nút chọn vai trò
            RoleButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.send),
                subtitle = stringResource(R.string.from_this_phone),
                icon = { /*SendIcon() */},
                backgroundColor = Color(0xFF00C853), // Màu xanh lá cây
                onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = true)) }
            )

            Spacer(modifier = Modifier.height(64.dp))

            RoleButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.receive),
                subtitle = stringResource(R.string.on_this_phone),
                icon = { /*ReceiveIcon()*/ },
                backgroundColor = Color(0xFF2962FF), // Màu xanh dương
                onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = false)) }
            )

        }
    }
}

@Composable
fun RoleButton(
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
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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


@Composable
private fun LottieBackground() {
    /* val composition by rememberLottieComposition(
         // Thay R.raw.background_animation bằng tên file JSON của bạn
         spec = LottieCompositionSpec.RawRes(R.raw.background_animation)
     )
     LottieAnimation(
         composition = composition,
         iterations = LottieConstants.IterateForever,
         modifier = Modifier
             .fillMaxSize()
             .graphicsLayer(alpha = 0.3f) // Làm mờ animation đi
     )*/
}

// --- Các hàm tiện ích cho RoleButton ---
@Composable
fun SendIcon() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PhoneAndroid, contentDescription = stringResource(R.string.sender), tint = Color.White)
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(R.string.to),
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Icon(Icons.Default.PhoneAndroid, contentDescription = stringResource(R.string.receiver), tint = Color.White)
    }
}

@Composable
fun ReceiveIcon() {
    Icon(
        Icons.Default.PhoneAndroid,
        contentDescription = stringResource(R.string.receive),
        tint = Color.White,
        modifier = Modifier.size(28.dp)
    )
}


// --- Preview để xem trước giao diện ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PhoneCloneSelectRolePreview() {
    SmoothTransferTheme {
        val fakeAction = object : PhoneClone.PhoneCloneActions {
            override fun onEvent(event: PhoneClone.Event) {
                // Xử lý sự kiện ở đây
            }
        }
        PhoneCloneSelectRole(action = fakeAction)
    }
}

@Composable
fun PhoneTransferAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "phone_transfer_animation")

    // Animation cho mũi tên di chuyển từ trái qua phải (khoảng cách nhỏ hơn)
    val arrowOffsetX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 0),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow_movement"
    )

    // Animation cho độ sáng của mũi tên (pulse effect)
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    // Animation cho phone bên trái (nhấp nháy nhẹ)
    val leftPhoneScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "left_phone_scale"
    )

    // Animation cho phone bên phải (nhấp nháy nhẹ)
    val rightPhoneScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "right_phone_scale"
    )

    Row(
        modifier = Modifier
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Phone bên trái (Sender)
        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = stringResource(R.string.sender),
            tint = Color(0xFF00C853).copy(alpha = 0.5f),
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = leftPhoneScale
                    scaleY = leftPhoneScale
                }
        )

        // Mũi tên chuyển động ở giữa (khoảng cách nhỏ)
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.to),
                tint = Color(0xFFA0A3AF).copy(alpha = 0.5f),
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = arrowOffsetX.dp)
            )
        }

        // Phone bên phải (Receiver)
        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = stringResource(R.string.receiver),
            tint = Color(0xFF2962FF).copy(alpha = 0.5f), // Màu xanh dương
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = rightPhoneScale
                    scaleY = rightPhoneScale
                }
        )
    }
}

