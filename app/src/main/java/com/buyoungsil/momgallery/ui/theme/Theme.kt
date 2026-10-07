package com.buyoungsil.momgallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 사이트와 같은 색
private val LightColors = lightColorScheme(
    background = Color(0xFFEDEFEC),
    onBackground = Color(0xFF16201A),
    surface = Color(0xFFEDEFEC),
    onSurface = Color(0xFF16201A),
    surfaceVariant = Color(0xFFDDE2DE),
    onSurfaceVariant = Color(0xFF5C6860),
    primary = Color(0xFF28508C),
    onPrimary = Color.White,
    outline = Color(0xFFCDD4CF),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF151514),
    onBackground = Color(0xFFE9E8E4),
    surface = Color(0xFF151514),
    onSurface = Color(0xFFE9E8E4),
    surfaceVariant = Color(0xFF2B2B29),
    onSurfaceVariant = Color(0xFF9C9B95),
    primary = Color(0xFFAAB8D4),
    onPrimary = Color(0xFF151514),
    outline = Color(0xFF2B2B29),
    error = Color(0xFFF2B8B5),
)

private val Serif = FontFamily.Serif

// 어르신용: 기본 글자를 크게
private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Light, fontSize = 34.sp, lineHeight = 46.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 40.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 34.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
)

// 화면 간격과 버튼 크기 기준값
object Dimens {
    val ScreenPadding = 20.dp
    val MinTouch = 60.dp      // 버튼 최소 높이
    val Gap = 16.dp
}

@Composable
fun MomGalleryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}