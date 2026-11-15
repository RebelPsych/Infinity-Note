package com.example.data

import androidx.compose.ui.graphics.Color

data class NoteColorOption(
  val index: Int,
  val label: String,
  val darkBackground: Color,
  val darkBorder: Color,
  val lightBackground: Color,
  val lightBorder: Color,
  val swatchColor: Color,
) {
  fun background(isDark: Boolean): Color = if (isDark) darkBackground else lightBackground

  fun border(isDark: Boolean): Color = if (isDark) darkBorder else lightBorder
}

object NoteColorPalette {
  val options: List<NoteColorOption> =
    listOf(
      NoteColorOption(
        index = 0,
        label = "Default",
        darkBackground = Color(0xFF15151E),
        darkBorder = Color(0xFF282738),
        lightBackground = Color(0xFFFFFFFF),
        lightBorder = Color(0xFFE5E0F0),
        swatchColor = Color(0xFF6B6880),
      ),
      NoteColorOption(
        index = 1,
        label = "Amethyst",
        darkBackground = Color(0xFF241736),
        darkBorder = Color(0xFF472C6B),
        lightBackground = Color(0xFFF3E8FF),
        lightBorder = Color(0xFFD8B4FE),
        swatchColor = Color(0xFFA855F7),
      ),
      NoteColorOption(
        index = 2,
        label = "Midnight",
        darkBackground = Color(0xFF142038),
        darkBorder = Color(0xFF273F6E),
        lightBackground = Color(0xFFE7F0FF),
        lightBorder = Color(0xFFBAD5FF),
        swatchColor = Color(0xFF3B82F6),
      ),
      NoteColorOption(
        index = 3,
        label = "Sage",
        darkBackground = Color(0xFF132620),
        darkBorder = Color(0xFF234A3D),
        lightBackground = Color(0xFFE6F7EF),
        lightBorder = Color(0xFFB3E5CE),
        swatchColor = Color(0xFF10B981),
      ),
      NoteColorOption(
        index = 4,
        label = "Amber",
        darkBackground = Color(0xFF291F13),
        darkBorder = Color(0xFF523C22),
        lightBackground = Color(0xFFFEF3E0),
        lightBorder = Color(0xFFF6D9A8),
        swatchColor = Color(0xFFF59E0B),
      ),
      NoteColorOption(
        index = 5,
        label = "Rose",
        darkBackground = Color(0xFF2D1623),
        darkBorder = Color(0xFF592944),
        lightBackground = Color(0xFFFFEAF2),
        lightBorder = Color(0xFFFBC5DA),
        swatchColor = Color(0xFFF43F5E),
      ),
      NoteColorOption(
        index = 6,
        label = "Slate",
        darkBackground = Color(0xFF1D222B),
        darkBorder = Color(0xFF343D4D),
        lightBackground = Color(0xFFEDF1F7),
        lightBorder = Color(0xFFD1DAE6),
        swatchColor = Color(0xFF64748B),
      ),
    )

  fun forIndex(index: Int): NoteColorOption = options.getOrElse(index) { options.first() }
}
