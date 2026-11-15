package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = DeepPurple,
    onPrimaryContainer = SoftLilac,
    secondary = VividPurple,
    onSecondary = Color.White,
    background = ObsidianBg,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = ObsidianTextSecondary,
    outline = ObsidianBorder,
    error = DeleteCoral,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = VividPurple,
    onPrimary = Color.White,
    primaryContainer = SoftLilac,
    onPrimaryContainer = DeepPurple,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    background = PaperBg,
    onBackground = PaperTextPrimary,
    surface = PaperSurface,
    onSurface = PaperTextPrimary,
    surfaceVariant = PaperSurfaceElevated,
    onSurfaceVariant = PaperTextSecondary,
    outline = PaperBorder,
    error = DeleteCoral,
    onError = Color.White,
  )

val InfinityShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
  )

@Composable
fun InfinityNoteTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = InfinityTypography,
    shapes = InfinityShapes,
    content = content,
  )
}
