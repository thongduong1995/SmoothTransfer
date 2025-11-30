package com.example.smoothtransfer.ui.phoneclone.screens

// Thêm import cho icon Android
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.R
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.phoneclone.animation.PhoneCloneBackgroundEffect
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
                title = stringResource(R.string.old_phone),
                subtitle = stringResource(R.string.from_this_phone),
                icon = { /*SendIcon() */},
                backgroundColor =
                    Brush.horizontalGradient (
                        listOf(
                            Color(0xFF4FACFE),
                            Color(0xFF00C6FB),
                        )
                    )
                , // Màu xanh lá cây
                onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = true)) },
                isSender = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoleButton(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.new_phone),
                subtitle = stringResource(R.string.on_this_phone),
                icon = { /*ReceiveIcon()*/ },
                backgroundColor =
                    Brush.horizontalGradient (
                        listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF8BC34A),
                        )
                    ), // Màu xanh dương
                onClick = { action.onEvent(PhoneClone.Event.RoleSelected(isSender = false)) },
                isSender = false
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
    backgroundColor: Brush,
    onClick: () -> Unit,
    isSender: Boolean = true
) {
    // --- 1. Tạo InteractionSource để lắng nghe sự kiện nhấn/nhả ---
    val interactionSource = remember { MutableInteractionSource() }
    // `isPressed` bây giờ sẽ tự động cập nhật khi có tương tác nhấn/nhả
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- 2. Animation thu nhỏ vẫn giữ nguyên, nhưng giờ sẽ dựa vào isPressed mới ---
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f, // Thu nhỏ mạnh hơn một chút
        animationSpec = tween(durationMillis = 200),
        label = "button_press_scale"
    )

    Box(
        modifier = modifier
            // --- 3. Áp dụng animation scale bằng graphicsLayer ---
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(24.dp)) // Bo tròn lớn hơn cho đẹp
            .background(backgroundColor)
            // --- 4. Sử dụng Modifier.clickable với InteractionSource ---
            // Thao tác này sẽ tự động cung cấp hiệu ứng gợn sóng (ripple)
            // và cập nhật `isPressed` cho chúng ta.
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Tắt hiệu ứng gợn sóng mặc định để tùy chỉnh
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Lớp phủ tối màu khi nhấn
        AnimatedVisibility(
            visible = isPressed,
            enter = fadeIn(animationSpec = tween(100)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }

 /*       // Hiệu ứng gợn sóng tùy chỉnh bên trên lớp phủ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                   // indication = rememberRipple(color = Color.White), // Hiệu ứng gợn sóng màu trắng
                    onClick = { *//* để trống vì đã xử lý ở ngoài *//* }
                )
        )*/

        // --- 5. Nội dung của nút không thay đổi ---
        PhoneCloneBackgroundEffect(isSender)
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

