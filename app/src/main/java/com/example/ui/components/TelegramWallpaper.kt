package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors

@Composable
fun TelegramWallpaperBackground(
    modifier: Modifier = Modifier,
    overrideStyle: Int? = null,
    content: @Composable () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val wallpaperStyle = overrideStyle ?: LocalAppearanceSettings.current.wallpaperStyle
    val isDark = tgColors.isDark

    val bgBrush = when (wallpaperStyle) {
        1 -> if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF141129), Color(0xFF1E183C), Color(0xFF121024)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFE6E0FA), Color(0xFFF3E5F5), Color(0xFFDFD8F7)))
        }
        2 -> if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF0A1D19), Color(0xFF102A24), Color(0xFF081714)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFCDE9DF), Color(0xFFDCF2E8), Color(0xFFC2E3D4)))
        }
        3 -> if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF211419), Color(0xFF2E1B20), Color(0xFF1B1115)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFFBE2D9), Color(0xFFFFEFE6), Color(0xFFF5D8CC)))
        }
        4 -> if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF0D141C), Color(0xFF121B26), Color(0xFF0C1219)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFE9EFF5), Color(0xFFF4F7FA), Color(0xFFE3EAF2)))
        }
        else -> if (isDark) {
            Brush.verticalGradient(listOf(Color(0xFF0E1621), Color(0xFF121C29), Color(0xFF0D1520)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFFCDE0C6), Color(0xFFDCE8D2), Color(0xFFC5DBCA)))
        }
    }

    val doodleColor = if (isDark) {
        Color.White.copy(alpha = if (wallpaperStyle == 4) 0.02f else 0.042f)
    } else {
        Color(0xFF2E4D37).copy(alpha = if (wallpaperStyle == 4) 0.04f else 0.08f)
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = bgBrush)

            val stepX = 138f
            val stepY = 138f
            var row = 0
            var y = 36f
            while (y < size.height + stepY) {
                var col = 0
                var x = if (row % 2 == 0) 28f else 92f
                while (x < size.width + stepX) {
                    when ((row * 3 + col) % 4) {
                        0 -> rotate(degrees = -18f, pivot = Offset(x, y)) {
                            val path = Path().apply {
                                moveTo(x - 14f, y)
                                lineTo(x + 14f, y - 10f)
                                lineTo(x + 4f, y + 12f)
                                lineTo(x - 2f, y + 3f)
                                close()
                            }
                            drawPath(path, color = doodleColor, style = Stroke(width = 2f))
                        }
                        1 -> drawCircle(
                            color = doodleColor,
                            radius = 11f,
                            center = Offset(x, y),
                            style = Stroke(width = 2f),
                        )
                        2 -> {
                            val p = Path().apply {
                                moveTo(x, y - 12f)
                                lineTo(x + 3f, y - 3f)
                                lineTo(x + 12f, y)
                                lineTo(x + 3f, y + 3f)
                                lineTo(x, y + 12f)
                                lineTo(x - 3f, y + 3f)
                                lineTo(x - 12f, y)
                                lineTo(x - 3f, y - 3f)
                                close()
                            }
                            drawPath(p, color = doodleColor, style = Stroke(width = 1.8f))
                        }
                        else -> drawCircle(
                            color = doodleColor,
                            radius = 5.5f,
                            center = Offset(x, y),
                        )
                    }
                    x += stepX
                    col++
                }
                y += stepY
                row++
            }
        }
        content()
    }
}
