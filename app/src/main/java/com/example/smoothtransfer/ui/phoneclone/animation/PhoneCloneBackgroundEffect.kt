package com.example.smoothtransfer.ui.phoneclone.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@Composable
fun PhoneCloneBackgroundEffect(isSender: Boolean = true) {
    // --- 1. TẠO ANIMATION CHO MŨI TÊN ---
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_animation")

    // Animate vị trí X của mũi tên
    val arrowTranslateX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "arrow_translate_x"
    )

    // Animate độ mờ của mũi tên để tạo hiệu ứng "pulse"
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f, // Bắt đầu trong suốt
        targetValue = 0.5f, // Hiện rõ ở giữa
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse // Đảo ngược animation (hiện -> mờ -> hiện)
        ),
        label = "arrow_alpha"
    )

    // --- 2. CHUẨN BỊ PAINTER CHO CẢ HAI LOẠI MŨI TÊN ---
    val forwardArrowPainter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ArrowForward)

    Canvas(modifier = Modifier.fillMaxSize()) {

        val canvasWidth = size.width
        val canvasHeight = size.height

        // ✅ 2 HÌNH TRÒN MỜ BÊN PHẢI
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = canvasWidth * 0.45f,
            center = Offset(canvasWidth * 1.05f, canvasHeight * 0.1f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.06f),
            radius = canvasWidth * 0.30f,
            center = Offset(canvasWidth * 0.85f, canvasHeight * 0.95f)
        )

        // --- BẮT ĐẦU VẼ ĐIỆN THOẠI ---

        // 1. Định nghĩa các thông số của điện thoại
        val phoneTopLeftX = canvasWidth * 0.07f
        val phoneTopLeftY = canvasHeight * 0.07f
        val phoneHeight = canvasHeight * 0.85f
        val phoneWidth = phoneHeight * 0.5f
        val phoneCornerRadius = CornerRadius(phoneWidth * 0.15f)

        // 2. ✅ VẼ THÂN ĐIỆN THOẠI
        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            topLeft = Offset(phoneTopLeftX, phoneTopLeftY),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = phoneCornerRadius
        )

        // 3. ✅ VẼ CAMERA "NỐT RUỒI"
        val cameraRadius = phoneWidth * 0.05f
        val cameraCenterX = phoneTopLeftX + phoneWidth / 2
        val cameraCenterY = phoneTopLeftY + (phoneHeight * 0.05f)
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = cameraRadius,
            center = Offset(cameraCenterX, cameraCenterY)
        )

        // =====================================================================
        // >>> BẮT ĐẦU VẼ CÁC THÀNH PHẦN MỚI CỦA HOME SCREEN <<<
        // =====================================================================

        val contentStartX = phoneTopLeftX + phoneWidth * 0.1f
        val contentEndX = phoneTopLeftX + phoneWidth * 0.9f
        val contentWidth = contentEndX - contentStartX

        // --- 4. VẼ WIDGET THỜI TIẾT VÀ LỊCH ---
        val widgetY = phoneTopLeftY + phoneHeight * 0.15f
        val weatherIconSize = contentWidth * 0.25f

        // Icon mặt trời (Thời tiết)
        drawCircle(
            color = Color(0xFFFFC107), // Màu vàng nắng
            radius = weatherIconSize / 2,
            center = Offset(contentStartX + weatherIconSize / 2, widgetY)
        )
        // Tia nắng
        for (i in 0..7) {
            rotate(degrees = i * 45f, pivot = Offset(contentStartX + weatherIconSize / 2, widgetY)) {
                drawLine(
                    color = Color(0xFFFFC107),
                    start = Offset(contentStartX + weatherIconSize / 2, widgetY - weatherIconSize * 0.6f),
                    end = Offset(contentStartX + weatherIconSize / 2, widgetY - weatherIconSize * 0.8f),
                    strokeWidth = weatherIconSize * 0.1f,
                    cap = StrokeCap.Round
                )
            }
        }
        // Icon lịch (bên phải)
        val calendarIconSize = contentWidth * 0.25f
        val calendarX = contentEndX - calendarIconSize
        // Thân lịch
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(calendarX, widgetY - calendarIconSize / 2),
            size = Size(calendarIconSize, calendarIconSize),
            cornerRadius = CornerRadius(calendarIconSize * 0.15f)
        )
        // Phần gáy trên
        drawRect(
            color = Color(0xFFE53935).copy(alpha = 0.8f ), // Màu đỏ
            topLeft = Offset(calendarX, widgetY - calendarIconSize / 2),
            size = Size(calendarIconSize, calendarIconSize * 0.25f)
        )

        // --- 2. VẼ 4 ICON APP Ở GIỮA --- (Được đưa lên trước)
        val appRowY = widgetY + phoneHeight * 0.15f // Tính toán vị trí Y cho hàng icon app
        val appIconSize = contentWidth * 0.2f
        val appIconSpacing = (contentWidth - (appIconSize * 4)) / 3f

        for (i in 0..3) {
            val appIconX = contentStartX + (appIconSize + appIconSpacing) * i
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(appIconX, appRowY),
                size = Size(appIconSize, appIconSize),
                cornerRadius = CornerRadius(appIconSize * 0.25f) // Bo góc vuông
            )
        }

        // --- 3. VẼ THANH GOOGLE SEARCH --- (Được di chuyển xuống dưới hàng icon app)
        val searchBarY = appRowY + phoneHeight * 0.12f // Tính toán vị trí Y mới, nằm dưới hàng icon app
        val searchBarHeight = phoneHeight * 0.06f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(contentStartX, searchBarY),
            size = Size(contentWidth, searchBarHeight),
            cornerRadius = CornerRadius(searchBarHeight) // Bo tròn tối đa
        )
        // Icon "G" của Google
        drawCircle(
            color = Color(0xFF4285F4),
            radius = searchBarHeight * 0.3f,
            center = Offset(contentStartX + searchBarHeight / 2, searchBarY + searchBarHeight / 2)
        )
        // Icon mic
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = searchBarHeight * 0.1f,
            center = Offset(contentEndX - searchBarHeight / 2, searchBarY + searchBarHeight / 2)
        )

        // --- 7. VẼ THANH DOCK VÀ 4 ICON Ở DƯỚI ---
        val dockHeight = phoneHeight * 0.15f
        val dockCornerRadius = phoneCornerRadius.x * 0.9f
        val dockIconSize = phoneWidth * 0.18f
        val dockIconSpacing = (phoneWidth - (dockIconSize * 4)) / 5f

        // Vẽ thanh Dock
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(
                x = phoneTopLeftX + dockIconSpacing / 2,
                y = phoneTopLeftY + phoneHeight - dockHeight - (dockIconSpacing / 2)
            ),
            size = Size(width = phoneWidth - dockIconSpacing, height = dockHeight),
            cornerRadius = CornerRadius(dockCornerRadius)
        )
        // Vẽ 4 icon trên Dock
        val dockIconY = phoneTopLeftY + phoneHeight - dockHeight / 2 - dockIconSpacing / 2
        val drawDockIcon = { index: Int, color: Color ->
            drawCircle(
                color = color,
                radius = dockIconSize / 2,
                center = Offset(
                    x = phoneTopLeftX + (dockIconSpacing * (index + 1)) + (dockIconSize * index) + (dockIconSize / 2),
                    y = dockIconY
                )
            )
        }
        drawDockIcon(0, Color(0xFF4CAF50))
        drawDockIcon(1, Color(0xFF2196F3))
        drawDockIcon(2, Color(0xFFF4B400))
        drawDockIcon(3, Color(0xFF9C27B0).copy(alpha = 0.8f))

        // --- VẼ MŨI TÊN CHUYỂN ĐỘNG ---

        // Tính toán vị trí Y để mũi tên nằm giữa cạnh phải của điện thoại
        val arrowCenterY = phoneTopLeftY + phoneHeight / 2
        val arrowSize = 80f

        // >>> LOGIC ĐIỀU KHIỂN VỊ TRÍ VÀ HƯỚNG MŨI TÊN DỰA VÀO isSender <<<
        if (isSender) {
            // SENDER: Mũi tên chạy bên cạnh phải, hướng sang phải
            translate(
                left = phoneTopLeftX + phoneWidth + arrowTranslateX, // Cạnh phải + hiệu ứng
                top = arrowCenterY - arrowSize / 2
            ) {
                with(forwardArrowPainter) { // Dùng mũi tên hướng sang phải
                    draw(
                        size = Size(arrowSize, arrowSize),
                        alpha = arrowAlpha,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        } else {
            // RECEIVER: Mũi tên chạy bên cạnh trái, hướng sang trái
            translate(
                left = phoneTopLeftX - arrowSize + arrowTranslateX, // Cạnh trái - kích thước mũi tên + hiệu ứng
                top = arrowCenterY - arrowSize / 2
            ) {
                with(forwardArrowPainter) { // Dùng mũi tên hướng sang trái
                    draw(
                        size = Size(arrowSize, arrowSize),
                        alpha = arrowAlpha,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhoneCloneBackgroundEffectPreview() {
    // Bọc trong theme của ứng dụng để preview có màu sắc đúng
    SmoothTransferTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Sử dụng màu nền gradient giống như màn hình RoleButton để xem hiệu ứng
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4FACFE),
                            Color(0xFF00C6FB)
                        )
                    )
                )
        ) {
            // Gọi Composable cần preview và cho nó chiếm toàn bộ không gian
            PhoneCloneBackgroundEffect()
        }
    }
}