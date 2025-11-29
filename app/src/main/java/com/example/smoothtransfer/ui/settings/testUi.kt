package com.example.smoothtransfer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    // DockedSearchBar quản lý trạng thái active (khi người dùng nhấn vào)
    var active by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        DockedSearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                onSearch(it)
                active = false // Đóng thanh search sau khi thực hiện tìm kiếm
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
            },
            trailingIcon = {
                // Hiển thị nút Clear khi có text
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            }
        ) {
            // Đây là khu vực hiển thị gợi ý tìm kiếm (khi active = true)
            // Bạn có thể thêm các gợi ý hoặc lịch sử tìm kiếm ở đây sau này
            // Ví dụ:
            // (0..4).forEach { idx ->
            //     val resultText = "Gợi ý $idx"
            //     ListItem(
            //         headlineContent = { Text(resultText) },
            //         modifier = Modifier.clickable {
            //             onQueryChange(resultText)
            //             active = false
            //         }
            //     )
            // }
        }
    }
}

// Preview để xem trước giao diện
@Preview(showBackground = true)
@Composable
private fun ModernSearchBarPreview() {
    var query by remember { mutableStateOf("") }
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModernSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    // Xử lý logic tìm kiếm ở đây
                    println("Searching for: $it")
                },
                placeholder = "Tìm kiếm ngôn ngữ..."
            )
        }
    }
}
