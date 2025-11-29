package com.example.smoothtransfer.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

/**
 * Một Composable Switch được thiết kế lại để giống với giao diện Samsung One UI.
 *
 * @param title Tiêu đề chính của hàng Switch.
 * @param summary Dòng mô tả phụ bên dưới tiêu đề.
 * @param checked Trạng thái bật/tắt của Switch.
 * @param onCheckedChange Callback được gọi khi người dùng nhấn vào Switch.
 * @param modifier Modifier để tùy chỉnh layout.
 */
@Composable
fun OneUiSwitch(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Đảm bảo Row có chiều cao tối thiểu để chứa các phần tử
            .clickable { onCheckedChange(!checked) } // Cho phép nhấn cả hàng để thay đổi
            .padding(horizontal = 20.dp, vertical = 14.dp), // Padding giống với One UI
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột chứa 2 dòng Text
        Column(
            modifier = Modifier
                .weight(1f) // Chiếm hết không gian còn lại
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface // Màu chữ chính
            )
            // Chỉ hiển thị dòng mô tả phụ nếu nó không rỗng
            if (!summary.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Màu chữ phụ (xám hơn)
                )
            }
        }

        // Switch đã được tùy chỉnh
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            // Thu nhỏ Switch một chút để giống với mẫu
            modifier = Modifier.scale(0.9f),
            colors = SwitchDefaults.colors(
                // Màu khi bật
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = Color.Transparent,
                // Màu khi tắt
                uncheckedThumbColor = Color(0xFFE0E0E0), // Màu thumb xám nhạt
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// --- Preview để xem trước giao diện ---
@Preview(showBackground = true, name = "One UI Switch Preview")
@Composable
fun OneUiSwitchPreview() {
    // State để điều khiển các Switch trong Preview
    var switch1 by remember { mutableStateOf(false) }
    var switch2 by remember { mutableStateOf(true) }

    // Sử dụng theme của bạn để Preview có màu sắc đúng
    SmoothTransferTheme(darkTheme = true) { // Bật darkTheme để giống ảnh mẫu
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Hàng Switch thứ nhất
                OneUiSwitch(
                    title = "Always On Display",
                    summary = "Show some info when the screen is off",
                    checked = switch1,
                    onCheckedChange = { switch1 = it }
                )
                // Hàng Switch thứ hai
                OneUiSwitch(
                    title = "Show unlock transition effect",
                    summary = null, // Không có dòng mô tả phụ
                    checked = switch2,
                    onCheckedChange = { switch2 = it }
                )
            }
        }
    }
}
