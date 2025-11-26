package com.example.smoothtransfer.ui.phoneclone.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.R
import com.example.smoothtransfer.network.protocol.DeviceInfo
import com.example.smoothtransfer.ui.phoneclone.PhoneClone
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@Composable
fun SearchingContentScreen(
    action: PhoneClone.PhoneCloneActions,
    deviceInfo: DeviceInfo = DeviceInfo("", "")
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))

            LoadingDotsIndicator()

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = stringResource(R.string.connected_to, deviceInfo.deviceName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.searching_for_data),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition("LoadingDots")
    val circleColor = MaterialTheme.colorScheme.primary

    @Composable
    fun Dot(alpha: Float) {
        Spacer(
            modifier = Modifier
                .size(20.dp)
                .alpha(alpha)
                .background(color = circleColor, shape = CircleShape)
        )
    }

    val alphas = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500
                    0f at 0
                    1f at (300 * index)
                    1f at (300 * index + 300)
                    0f at (300 * index + 900)
                    0f at 1500
                },
            ),
            label = "DotAlpha$index"
        ).value
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        alphas.forEach { alpha ->
            Dot(alpha = alpha)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchingContentScreenPreview() {
    SmoothTransferTheme {
        val fakeAction = object : PhoneClone.PhoneCloneActions {
            override fun onEvent(event: PhoneClone.Event) {}
        }
        val fakeDeviceInfo = DeviceInfo(deviceName = "Galaxy S24 Ultra", ipAddress = "192.168.1.100")
        SearchingContentScreen(action = fakeAction, deviceInfo = fakeDeviceInfo)
    }
}
