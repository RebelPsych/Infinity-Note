package com.example.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMono

data class MarkdownHighlightColors(
  val syntaxMarker: Color,
  val accent: Color,
  val codeBackground: Color,
  val quoteText: Color,
)

class MarkdownVisualTransformation(private val colors: MarkdownHighlightColors) :
  VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val raw = text.text
    if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

    val builder = AnnotatedString.Builder(raw)
    applyBlockStyles(raw, builder)
    applyInlineStyles(raw, builder)
    return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
  }

  private fun applyBlockStyles(raw: String, builder: AnnotatedString.Builder) {
    var lineStart = 0
    val len = raw.length
    while (lineStart <= len) {
      val nextNewline = raw.indexOf('\n', lineStart)
      val lineEnd = if (nextNewline == -1) len else nextNewline
      if (lineEnd > lineStart) {
        val line = raw.substring(lineStart, lineEnd)
        when {
          line.startsWith("### ") -> {
            builder.addStyle(
              SpanStyle(color = colors.syntaxMarker, fontWeight = FontWeight.Medium),
              lineStart,
              lineStart + 4,
            )
            builder.addStyle(
              SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
              lineStart,
              lineEnd,
            )
          }
          line.startsWith("## ") -> {
            builder.addStyle(
              SpanStyle(color = colors.syntaxMarker, fontWeight = FontWeight.Medium),
              lineStart,
              lineStart + 3,
            )
            builder.addStyle(
              SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
              lineStart,
              lineEnd,
            )
          }
          line.startsWith("# ") -> {
            builder.addStyle(
              SpanStyle(color = colors.syntaxMarker, fontWeight = FontWeight.Medium),
              lineStart,
              lineStart + 2,
            )
            builder.addStyle(
              SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
              lineStart,
              lineEnd,
            )
          }
          line.startsWith("> ") -> {
            builder.addStyle(
              SpanStyle(color = colors.accent, fontWeight = FontWeight.Bold),
              lineStart,
              lineStart + 2,
            )
            builder.addStyle(
              SpanStyle(color = colors.quoteText, fontStyle = FontStyle.Italic),
              lineStart + 2,
              lineEnd,
            )
          }
          line.startsWith("- [x] ") || line.startsWith("- [X] ") || line.startsWith("- [ ] ") -> {
            builder.addStyle(
              SpanStyle(
                color = colors.accent,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono,
              ),
              lineStart,
              lineStart + 6,
            )
            if (line[3] == 'x' || line[3] == 'X') {
              builder.addStyle(
                SpanStyle(
                  color = colors.syntaxMarker,
                  textDecoration = TextDecoration.LineThrough,
                ),
                lineStart + 6,
                lineEnd,
              )
            }
          }
          line.startsWith("- ") || line.startsWith("* ") -> {
            builder.addStyle(
              SpanStyle(color = colors.accent, fontWeight = FontWeight.Bold),
              lineStart,
              lineStart + 2,
            )
          }
          line == "---" || line == "***" -> {
            builder.addStyle(
              SpanStyle(color = colors.syntaxMarker, fontWeight = FontWeight.Bold),
              lineStart,
              lineEnd,
            )
          }
          else -> {
            val orderedMatch = ORDERED_LIST_PREFIX.find(line)
            if (orderedMatch != null) {
              builder.addStyle(
                SpanStyle(color = colors.accent, fontWeight = FontWeight.SemiBold),
                lineStart,
                lineStart + orderedMatch.value.length,
              )
            }
          }
        }
      }
      if (nextNewline == -1) break
      lineStart = nextNewline + 1
    }
  }

  private fun applyInlineStyles(raw: String, builder: AnnotatedString.Builder) {
    INLINE_CODE_REGEX.findAll(raw).forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1
      if (end - start >= 2) {
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), start, start + 1)
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), end - 1, end)
        builder.addStyle(
          SpanStyle(
            fontFamily = JetBrainsMono,
            background = colors.codeBackground,
            fontSize = 14.sp,
          ),
          start,
          end,
        )
      }
    }

    BOLD_REGEX.findAll(raw).forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1
      if (end - start > 4) {
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), start, start + 2)
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), end - 2, end)
        builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start + 2, end - 2)
      }
    }

    ITALIC_STAR_REGEX.findAll(raw).forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1
      if (end - start > 2) {
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), start, start + 1)
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), end - 1, end)
        builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start + 1, end - 1)
      }
    }

    STRIKE_REGEX.findAll(raw).forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1
      if (end - start > 4) {
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), start, start + 2)
        builder.addStyle(SpanStyle(color = colors.syntaxMarker), end - 2, end)
        builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start + 2, end - 2)
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MarkdownVisualTransformation) return false
    return colors == other.colors
  }

  override fun hashCode(): Int = colors.hashCode()

  private companion object {
    val ORDERED_LIST_PREFIX = Regex("^\\d+\\.\\s")
    val INLINE_CODE_REGEX = Regex("`[^`\\n]+`")
    val BOLD_REGEX = Regex("\\*\\*[^*\\n]+\\*\\*")
    val ITALIC_STAR_REGEX = Regex("(?<!\\*)\\*(?!\\*)[^*\\n]+(?<!\\*)\\*(?!\\*)")
    val STRIKE_REGEX = Regex("~~[^~\\n]+~~")
  }
}

fun parseMarkdownPreview(
  markdown: String,
  accentColor: Color,
  codeBackground: Color,
  mutedColor: Color,
  codeFontFamily: FontFamily = JetBrainsMono,
): AnnotatedString {
  if (markdown.isBlank()) return AnnotatedString("")

  return buildAnnotatedString {
    val lines = markdown.lines().take(8)
    lines.forEachIndexed { idx, rawLine ->
      val trimmed = rawLine.trimEnd()
      when {
        trimmed.startsWith("### ") -> {
          pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp))
          appendInlinePreview(trimmed.drop(4), codeBackground, codeFontFamily, mutedColor)
          pop()
        }
        trimmed.startsWith("## ") -> {
          pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
          appendInlinePreview(trimmed.drop(3), codeBackground, codeFontFamily, mutedColor)
          pop()
        }
        trimmed.startsWith("# ") -> {
          pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp))
          appendInlinePreview(trimmed.drop(2), codeBackground, codeFontFamily, mutedColor)
          pop()
        }
        trimmed.startsWith("> ") -> {
          pushStyle(SpanStyle(color = mutedColor, fontStyle = FontStyle.Italic))
          append("│ ")
          appendInlinePreview(trimmed.drop(2), codeBackground, codeFontFamily, mutedColor)
          pop()
        }
        trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
          pushStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold))
          append("☑ ")
          pop()
          pushStyle(SpanStyle(color = mutedColor, textDecoration = TextDecoration.LineThrough))
          appendInlinePreview(trimmed.drop(6), codeBackground, codeFontFamily, mutedColor)
          pop()
        }
        trimmed.startsWith("- [ ] ") -> {
          pushStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold))
          append("☐ ")
          pop()
          appendInlinePreview(trimmed.drop(6), codeBackground, codeFontFamily, mutedColor)
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          pushStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold))
          append("• ")
          pop()
          appendInlinePreview(trimmed.drop(2), codeBackground, codeFontFamily, mutedColor)
        }
        else -> {
          appendInlinePreview(trimmed, codeBackground, codeFontFamily, mutedColor)
        }
      }
      if (idx < lines.lastIndex) {
        append('\n')
      }
    }
  }
}

private fun AnnotatedString.Builder.appendInlinePreview(
  text: String,
  codeBackground: Color,
  codeFontFamily: FontFamily,
  mutedColor: Color,
) {
  var i = 0
  val len = text.length
  while (i < len) {
    when {
      text.startsWith("**", i) -> {
        val close = text.indexOf("**", i + 2)
        if (close != -1) {
          pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
          append(text.substring(i + 2, close))
          pop()
          i = close + 2
        } else {
          append(text[i])
          i++
        }
      }
      text.startsWith("~~", i) -> {
        val close = text.indexOf("~~", i + 2)
        if (close != -1) {
          pushStyle(SpanStyle(color = mutedColor, textDecoration = TextDecoration.LineThrough))
          append(text.substring(i + 2, close))
          pop()
          i = close + 2
        } else {
          append(text[i])
          i++
        }
      }
      text[i] == '`' -> {
        val close = text.indexOf('`', i + 1)
        if (close != -1) {
          pushStyle(
            SpanStyle(
              fontFamily = codeFontFamily,
              background = codeBackground,
              fontSize = 13.sp,
            )
          )
          append(text.substring(i + 1, close))
          pop()
          i = close + 1
        } else {
          append(text[i])
          i++
        }
      }
      text[i] == '*' -> {
        val close = text.indexOf('*', i + 1)
        if (close != -1 && close > i + 1) {
          pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
          append(text.substring(i + 1, close))
          pop()
          i = close + 1
        } else {
          append(text[i])
          i++
        }
      }
      else -> {
        append(text[i])
        i++
      }
    }
  }
}
