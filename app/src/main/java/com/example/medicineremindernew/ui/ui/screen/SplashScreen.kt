package com.example.medicineremindernew.ui.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.Krem


@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        visible = false
        kotlinx.coroutines.delay(500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BiruTua.copy(alpha = 1.0f))
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(
                "MedTime",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                ),
                color = Krem.copy(alpha = 1.0f)
            )
        }
    }
}
