package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NoteColorPalette
import com.example.markdown.MarkdownHighlightColors
import com.example.markdown.MarkdownVisualTransformation
import com.example.ui.components.InfinitySymbol
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.VividPurple

@Composable
fun NoteEditorScreen(
  editorState: EditorUiState,
  onTitleChanged: (String) -> Unit,
  onContentChanged: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  val palette = NoteColorPalette.forIndex(editorState.colorIndex)
  val animatedBg by
    animateColorAsState(
      targetValue = palette.background(isDark),
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "note_bg_color",
    )

  var isMenuExpanded by remember(editorState.sessionKey) { mutableStateOf(false) }
  var isColorPickerOpen by remember(editorState.sessionKey) { mutableStateOf(false) }

  var titleField by
    remember(editorState.sessionKey) {
      mutableStateOf(TextFieldValue(editorState.title, TextRange(editorState.title.length)))
    }
  var contentField by
    remember(editorState.sessionKey) {
      mutableStateOf(TextFieldValue(editorState.content, TextRange(editorState.content.length)))
    }

  LaunchedEffect(editorState.historyVersion) {
    if (titleField.text != editorState.title) {
      titleField = TextFieldValue(editorState.title, TextRange(editorState.title.length))
    }
    if (contentField.text != editorState.content) {
      contentField = TextFieldValue(editorState.content, TextRange(editorState.content.length))
    }
  }

  val contentFocusRequester = remember { FocusRequester() }
  val scrollState = rememberScrollState()

  val markdownTransformation =
    remember(isDark) {
      MarkdownVisualTransformation(
        MarkdownHighlightColors(
          syntaxMarker =
            if (isDark) Color.White.copy(alpha = 0.36f) else Color.Black.copy(alpha = 0.36f),
          accent = ElectricPurple,
          codeBackground =
            if (isDark) Color.White.copy(alpha = 0.11f) else Color.Black.copy(alpha = 0.07f),
          quoteText =
            if (isDark) Color.White.copy(alpha = 0.72f) else Color.Black.copy(alpha = 0.68f),
        )
      )
    }

  BackHandler {
    if (isMenuExpanded) {
      isMenuExpanded = false
      isColorPickerOpen = false
    } else {
      onBack()
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(animatedBg)
        .windowInsetsPadding(WindowInsets.statusBars)
        .navigationBarsPadding()
        .imePadding()
  ) {
    // Clean Note Page: Title field + empty field for notes
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(scrollState)
          .padding(top = 82.dp, start = 22.dp, end = 22.dp, bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        // Title Field
        Box(modifier = Modifier.fillMaxWidth()) {
          if (titleField.text.isEmpty()) {
            Text(
              text = stringResource(R.string.placeholder_title),
              style =
                MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )
          }
          BasicTextField(
            value = titleField,
            onValueChange = { next ->
              titleField = next
              onTitleChanged(next.text)
            },
            textStyle =
              MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              ),
            cursorBrush = SolidColor(ElectricPurple),
            keyboardOptions =
              KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
              ),
            keyboardActions = KeyboardActions(onNext = { contentFocusRequester.requestFocus() }),
            modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Note Content Field (Markdown live styling, tap anywhere below to focus)
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .defaultMinSize(minHeight = 420.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
              ) {
                contentFocusRequester.requestFocus()
              }
        ) {
          if (contentField.text.isEmpty()) {
            Text(
              text = stringResource(R.string.placeholder_content),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            )
          }
          BasicTextField(
            value = contentField,
            onValueChange = { next ->
              contentField = next
              onContentChanged(next.text)
            },
            textStyle =
              MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(ElectricPurple),
            visualTransformation = markdownTransformation,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier =
              Modifier.fillMaxWidth()
                .defaultMinSize(minHeight = 420.dp)
                .focusRequester(contentFocusRequester)
                .testTag("note_content_input"),
          )
        }
      }
    }

    // Dismiss menu when tapping outside while open
    if (isMenuExpanded) {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
            ) {
              isMenuExpanded = false
              isColorPickerOpen = false
            }
      )
    }

    // Upper-left Purple Infinity Symbol Menu Circle + expandable options
    Column(
      modifier = Modifier.align(Alignment.TopStart).padding(top = 12.dp, start = 16.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Purple infinity symbol menu circle
        Surface(
          onClick = {
            isMenuExpanded = !isMenuExpanded
            if (!isMenuExpanded) isColorPickerOpen = false
          },
          shape = CircleShape,
          color = Color.Transparent,
          shadowElevation = 8.dp,
          modifier =
            Modifier.size(50.dp)
              .minimumInteractiveComponentSize()
              .testTag("infinity_menu_button"),
        ) {
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.linearGradient(colors = listOf(ElectricPurple, VividPurple)),
                  shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
          ) {
            InfinitySymbol(
              color = Color.White,
              modifier = Modifier.size(28.dp),
              strokeWidthFactor = 0.14f,
            )
          }
        }

        // Expanded menu options: Back, Color, Undo, Redo
        AnimatedVisibility(
          visible = isMenuExpanded,
          enter =
            fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
              expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
          exit =
            fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
              shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium)),
        ) {
          Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 10.dp,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              IconButton(
                onClick = {
                  isMenuExpanded = false
                  isColorPickerOpen = false
                  onBack()
                },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("editor_back_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                  contentDescription = stringResource(R.string.action_back),
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }

              IconButton(
                onClick = { isColorPickerOpen = !isColorPickerOpen },
                modifier =
                  Modifier.minimumInteractiveComponentSize().testTag("editor_color_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Palette,
                  contentDescription = stringResource(R.string.action_color),
                  tint =
                    if (isColorPickerOpen) ElectricPurple else MaterialTheme.colorScheme.onSurface,
                )
              }

              IconButton(
                onClick = onUndo,
                enabled = editorState.canUndo,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("editor_undo_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.Undo,
                  contentDescription = stringResource(R.string.action_undo),
                  tint =
                    if (editorState.canUndo) {
                      MaterialTheme.colorScheme.onSurface
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                )
              }

              IconButton(
                onClick = onRedo,
                enabled = editorState.canRedo,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("editor_redo_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.Redo,
                  contentDescription = stringResource(R.string.action_redo),
                  tint =
                    if (editorState.canRedo) {
                      MaterialTheme.colorScheme.onSurface
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                )
              }
            }
          }
        }
      }

      // Color swatch picker row when Color option is selected
      AnimatedVisibility(
        visible = isMenuExpanded && isColorPickerOpen,
        enter =
          fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
            expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
        exit =
          fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
            shrinkVertically(spring(stiffness = Spring.StiffnessMedium)),
      ) {
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          shadowElevation = 10.dp,
          modifier = Modifier.padding(top = 10.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            NoteColorPalette.options.forEach { option ->
              val isSelected = option.index == editorState.colorIndex
              Box(
                modifier =
                  Modifier.minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable {
                      onColorSelected(option.index)
                      isColorPickerOpen = false
                    }
                    .testTag("color_swatch_${option.index}"),
                contentAlignment = Alignment.Center,
              ) {
                Box(
                  modifier =
                    Modifier.size(32.dp)
                      .clip(CircleShape)
                      .background(option.swatchColor)
                      .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color =
                          if (isSelected) Color.White else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                      ),
                  contentAlignment = Alignment.Center,
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Rounded.Check,
                      contentDescription = option.label,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
