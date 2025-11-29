package com.example.smoothtransfer.ui.phoneclone.screens

// Thêm import cho icon Android
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
    // --- 1. Tạo State và Animation ---
    // State để theo dõi trạng thái nhấn (pressed)
    var isPressed by remember { mutableStateOf(false) }
    // Tạo animation cho giá trị scale.
    // Nó sẽ tự động thay đổi mượt mà khi `isPressed` thay đổi.
    val scale by animateFloatAsState(
        // Nếu đang nhấn, thu nhỏ lại còn 95% (0.95f).
        // Nếu không nhấn, trở về kích thước gốc (1f).
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 300), // Animation diễn ra nhanh
        label = "button_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        // KHI BẮT ĐẦU NHẤN XUỐNG
                        isPressed = true // Kích hoạt animation thu nhỏ
                        try {
                            // Chờ cho đến khi người dùng nhả tay ra
                            awaitRelease()
                        } finally {
                            // KHI NHẢ TAY RA (hoặc cử chỉ bị hủy)
                            isPressed = false // Kích hoạt animation phóng to trở lại
                        }
                    },
                    onTap = {
                        // KHI MỘT CỬ CHỈ "TAP" HOÀN CHỈNH (NHẤN XUỐNG VÀ NHẢ RA)
                        // ĐƯỢC GHI NHẬN, HÀNH ĐỘNG onClick SẼ ĐƯỢC GỌI NGAY LẬP TỨC.
                        onClick()
                    }
                )
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.8f),// Màu của bóng môi trường (thường là đen)
                spotColor = Color.Black.copy(alpha = 0.8f) // àu của bóng từ nguồn sáng (thường là đen)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            //.clickable(onClick = onClick)
            .padding(4.dp),
            contentAlignment = Alignment.Center
    ) {
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

@Composable
fun ModernPhoneCanvas(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val bodyCornerRadius = 24f
        val screenPadding = 12f
        val screenCornerRadius = 16f
        val punchHoleRadius = 8f
        val speakerHeight = 8f
        val speakerWidth = size.width * 0.3f

        // 1. Vẽ thân máy (vỏ ngoài) với màu được truyền vào
        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(bodyCornerRadius)
        )

        // 2. Vẽ màn hình bên trong (màu tối hơn một chút)
        drawRoundRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(screenPadding, screenPadding),
            size = Size(
                width = size.width - (screenPadding * 2),
                height = size.height - (screenPadding * 2)
            ),
            cornerRadius = CornerRadius(screenCornerRadius)
        )

        // 3. Vẽ "nốt ruồi" (punch-hole camera)
        drawCircle(
            color = color,
            radius = punchHoleRadius,
            center = Offset(x = size.width / 2, y = screenPadding + punchHoleRadius + 12f)
        )

        // 4. Vẽ loa thoại
        drawRoundRect(
            color = color,
            topLeft = Offset(x = (size.width - speakerWidth) / 2, y = screenPadding / 3),
            size = Size(speakerWidth, speakerHeight),
            cornerRadius = CornerRadius(speakerHeight / 2)
        )
    }
}

