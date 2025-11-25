package com.example.smoothtransfer.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.R
import com.example.smoothtransfer.data.local.LanguageManager
import com.example.smoothtransfer.data.local.ThemeManager
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChange: (Boolean) -> Unit = {}, // Callback khi thay đổi theme
    onLanguageChange: (String) -> Unit = {}, // Callback khi thay đổi ngôn ngữ
    modifier: Modifier = Modifier // Modifier mặc định là empty
) {
    // Lấy Context để sử dụng TimePickerDialog và ThemeManager
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguage = remember { LanguageManager.getCurrentLanguage(context) }
    val themeManager = remember { ThemeManager(context) }
    //val languageManager = remember { LanguageManager(context) }

    // State cho Light/Dark mode: true = Light mode, false = Dark mode
    // Đọc từ ThemeManager khi khởi tạo
    var isLightMode by remember {
        mutableStateOf(!themeManager.isDarkMode()) // isLightMode = !isDarkMode
    }

    // State cho Notification: true = bật, false = tắt
    var isNotificationEnabled by remember { mutableStateOf(true) }

    // Lấy thời gian hiện tại của phone và format thành "HH:mm"
    val currentTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    // State cho thời gian thông báo (mặc định là thời gian hiện tại của phone)
    var notificationTime by remember { mutableStateOf(currentTime) }

    Column(
        modifier = modifier
            .fillMaxSize() // Chiếm toàn bộ không gian
            .padding(top = 48.dp)
            .padding(16.dp), // Padding thêm 16dp
        verticalArrangement = Arrangement.spacedBy(4.dp) // Khoảng cách giữa các items
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), // Sử dụng outline color từ theme
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp), // Bo góc 16dp
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface // Sử dụng surface color từ theme
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Giữ elevation để có bóng
        ) {
            Column(
                modifier = Modifier.padding(16.dp), // Padding bên trong card
                verticalArrangement = Arrangement.spacedBy(4.dp) // Khoảng cách giữa các items
            ) {
                /**
                 * LIGHT/DARK MODE TOGGLE
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(), // Chiếm toàn bộ chiều rộng
                    horizontalArrangement = Arrangement.SpaceBetween, // Đẩy về 2 đầu
                    verticalAlignment = Alignment.CenterVertically // Căn giữa dọc
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, // Căn giữa dọc
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách 12dp
                    ) {
                        Icon(
                            imageVector = if (isLightMode) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = if (isLightMode) stringResource(R.string.light_mode) else stringResource(
                                R.string.dark_mode
                            ),
                            tint = Color(0xFFFF9800), // Màu cam (#FF9800)
                            modifier = Modifier.size(24.dp) // Kích thước 24x24dp
                        )
                        Text(
                            text = if (isLightMode) stringResource(R.string.light_mode) else stringResource(
                                R.string.dark_mode
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal, // Giảm độ đậm
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Sử dụng onSurface từ theme
                        )
                    }
                    Switch(
                        checked = isLightMode, // Trạng thái hiện tại
                        onCheckedChange = { newValue ->
                            isLightMode = newValue
                            val isDarkMode = !newValue // isDarkMode = !isLightMode
                            themeManager.setDarkMode(isDarkMode) // Lưu vào SharedPreferences
                            onThemeChange(isDarkMode) // Gọi callback để cập nhật theme toàn app
                        },
                        thumbContent = {
                            Icon(
                                imageVector = if (isLightMode) Icons.Default.WbSunny else Icons.Default.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = Color.White
                            )
                        },
                        modifier = Modifier.scale(
                            scaleX = 1f,
                            scaleY = 0.95f
                        ), // Giảm chiều cao hơn (giảm 15%)
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF737371)
                        )
                    )
                }

                // HorizontalDivider - Đường kẻ ngang để phân cách các sections
                HorizontalDivider()

                /**
                 * NOTIFICATIONS ROW
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Đẩy về 2 đầu
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f), // Chiếm không gian còn lại
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách 12dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color(0xFFFF9800), // Màu cam
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.notifications),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal, // Giảm độ đậm
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Sử dụng onSurface từ theme
                        )
                        Card(
                            onClick = {
                                val timeParts = notificationTime.split(":")
                                val currentHour =
                                    timeParts[0].toIntOrNull() ?: Calendar.getInstance()
                                        .get(Calendar.HOUR_OF_DAY)
                                val currentMinute =
                                    timeParts[1].toIntOrNull() ?: Calendar.getInstance()
                                        .get(Calendar.MINUTE)

                                android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val formattedTime = String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            hourOfDay,
                                            minute
                                        )
                                        notificationTime = formattedTime
                                    },
                                    currentHour, // Giờ hiện tại
                                    currentMinute, // Phút hiện tại
                                    true // 24-hour format
                                ).show()
                            },
                            modifier = Modifier
                                .width(110.dp)
                                .height(36.dp),
                            shape = RoundedCornerShape(12.dp), // Bo tròn nhiều hơn giống iOS
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Shadow nhẹ
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notificationTime,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = {
                            isNotificationEnabled = it
                        }, // Update state khi click
                        modifier = Modifier.scale(
                            scaleX = 1f,
                            scaleY = 0.95f
                        ), // Giảm chiều cao hơn (giảm 15%)
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF9800).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                    )
                }

                HorizontalDivider()

                /**
                 * LANGUAGE SELECTION
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.language),
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.your_language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal, // Giảm độ đậm
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Sử dụng onSurface từ theme
                        )

                        Box {
                            Card(
                                onClick = { showLanguageDialog = true },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(12.dp), // Bo tròn nhiều hơn giống iOS
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Shadow nhẹ
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentLanguage.displayName,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }


                // Language Selection Dialog
                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                        currentLanguage = currentLanguage,
                        onDismiss = { showLanguageDialog = false },
                        onLanguageSelected = { language ->
                            LanguageManager.updateAppLocale(context, language)
                            showLanguageDialog = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * LanguageSelectionDialog - Dialog for selecting app language
 *
 * Displays a list of available languages with radio buttons.
 * User can select a language and apply it to the app.
 *
 * @param currentLanguage Currently selected language
 * @param onDismiss Callback invoked when dialog is dismissed
 * @param onLanguageSelected Callback invoked when a language is selected
 */
@Composable
private fun LanguageSelectionDialog(
    currentLanguage: LanguageManager.Language,
    onDismiss: () -> Unit,
    onLanguageSelected: (LanguageManager.Language) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.select_language))
        },
        text = {
            Column {
                LanguageManager.Language.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLanguage = language }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = { selectedLanguage = language }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onLanguageSelected(selectedLanguage)
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SmoothTransferTheme {
        SettingsScreen(
            onThemeChange = {},
            onLanguageChange = {}
        )
    }
}
