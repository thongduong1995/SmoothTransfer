package com.example.smoothtransfer.ui.phoneclone.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smoothtransfer.ui.phoneclone.animation.PhoneCloneBackgroundEffect
import com.example.smoothtransfer.ui.theme.SmoothTransferTheme

@Composable
fun HomeScreen() {
    Column (modifier = Modifier.padding(16.dp).fillMaxSize()) {
        HomeScreenTest(onclick = {})
    }
}
@Composable
fun HomeScreenTest(
    onclick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = modifier
          .fillMaxSize()
          .height(140.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
              Brush.horizontalGradient(
                  listOf(
                      Color(0xFF4FACFE),
                      Color(0xFF00C6FB)
                  )
              )
          )
          .clickable { onclick() }
  ) {
      Row (modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
          Column (
              modifier = Modifier.weight(1f),
          ) {
              Text(
                text  = "Phone Mover",
                  color = Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                  text = "Migrate your all data to your new phone",
                  color = Color.White.copy(alpha = 0.9f),
                  fontSize = 13.sp
              )
          }
      }

      PhoneCloneBackgroundEffect()
  }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenTestPreview() {
    SmoothTransferTheme {
        HomeScreen()
    }
}
