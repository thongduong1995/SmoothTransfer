package com.example.smoothtransfer.ui.phoneclone.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme
import kotlinx.coroutines.launch

@Composable
fun ConnectingScreen(
    action: PhoneClone.PhoneCloneActions,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Icon "Link" ở trên cùng
        Icon(
            imageVector = Icons.Rounded.Link,
            contentDescription = "Connecting",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Dòng chữ "Connecting..."
        Text(
            text = "Connecting to transfer your data...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        // 3. Animation Vòng tròn xoay
        ConnectingAnimation()

        // Spacer chiếm phần không gian còn lại
        Spacer(modifier = Modifier.weight(1f))

        // 4. Có thể thêm nút Cancel ở đây nếu cần
        // Button(onClick = { action.onEvent(PhoneClone.Event.CancelConnection) }) {
        //     Text("Cancel")
        // }
    }
}

@Composable
private fun ConnectingAnimation() {
    val ringColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val phoneColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    val primaryColor = MaterialTheme.colorScheme.primary

    // Tạo hiệu ứng xoay vô hạn cho cả cụm
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation_angle"
    )

    // Tạo hiệu ứng cho "tia quét" trên vòng tròn
    val arcStartAngle = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        this.launch {
            arcStartAngle.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .rotate(rotationAngle), // Áp dụng hiệu ứng xoay
        contentAlignment = Alignment.Center
    ) {
        // Vòng tròn nền
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ringColor,
                style = Stroke(width = 15.dp.toPx())
            )
        }

        // "Tia quét" màu gradient trên vòng tròn
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor,
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = arcStartAngle.value,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Hình chữ nhật giả lập điện thoại ở giữa
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 120.dp)
                .background(
                    color = phoneColor,
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

// Preview để xem trước giao diện
@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun ConnectingScreenPreview() {
    SmoothTransferTheme {
        ConnectingScreen(
            action = object : PhoneClone.PhoneCloneActions {
                override fun onEvent(event: PhoneClone.Event) {}
            },
            message = "Connecting to transfer your data..."
        )
    }
}
