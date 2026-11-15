package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun InfinitySymbol(
  color: Color,
  modifier: Modifier = Modifier,
  strokeWidthFactor: Float = 0.14f,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val rx = w * 0.42f
    val ry = h * 0.27f

    val path =
      Path().apply {
        moveTo(cx, cy)
        cubicTo(
          cx - rx * 0.45f,
          cy - ry * 1.35f,
          cx - rx,
          cy - ry * 1.15f,
          cx - rx,
          cy,
        )
        cubicTo(
          cx - rx,
          cy + ry * 1.15f,
          cx - rx * 0.45f,
          cy + ry * 1.35f,
          cx,
          cy,
        )
        cubicTo(
          cx + rx * 0.45f,
          cy - ry * 1.35f,
          cx + rx,
          cy - ry * 1.15f,
          cx + rx,
          cy,
        )
        cubicTo(
          cx + rx,
          cy + ry * 1.15f,
          cx + rx * 0.45f,
          cy + ry * 1.35f,
          cx,
          cy,
        )
      }

    drawPath(
      path = path,
      color = color,
      style =
        Stroke(
          width = (minOf(w, h) * strokeWidthFactor).coerceAtLeast(2f),
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )
  }
}
