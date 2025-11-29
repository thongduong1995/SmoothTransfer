package com.example.smoothtransfer.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    // Animated color
    val switchColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = switchColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = switchColor.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomSwitchPreview() {
    var switchState by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CustomSwitch(
                checked = switchState,
                onCheckedChange = { switchState = it },
                label = "Bật/Tắt chế độ"
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomSwitch(
                checked = switchState,
                onCheckedChange = { switchState = it }
            )
        }
    }
}