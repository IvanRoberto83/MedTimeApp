package com.example.medicineremindernew.ui.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material3.ButtonDefaults

@Composable
fun AlarmPopupPreviewScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 4.dp,
                    color = Color(0xFF027A7E),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Saatnya Minum Obat!", fontSize = 22.sp, color=Color(0xFF011A27))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Obat: Paracetamol\nDosis: 2 tablet", fontSize = 16.sp, color=Color(0xFF011A27))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Tombol bisa dikosongkan dulu */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF027A7E),
                    contentColor = Color.White
                )
            ) {
                Text("Matikan Alarm")
            }
        }
    }
}
