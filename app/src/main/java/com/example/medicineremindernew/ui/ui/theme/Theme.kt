package com.example.medicineremindernew.ui.ui.theme

    import android.os.Build
    import androidx.compose.foundation.isSystemInDarkTheme
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.darkColorScheme
    import androidx.compose.material3.dynamicDarkColorScheme
    import androidx.compose.material3.dynamicLightColorScheme
    import androidx.compose.material3.lightColorScheme
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BiruTua,
    secondary = BiruMuda,
    background = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = BiruTua,
    secondary = BiruMuda,
    background = Color.White
)

@Composable
fun MedicineReminderNewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicDarkColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}