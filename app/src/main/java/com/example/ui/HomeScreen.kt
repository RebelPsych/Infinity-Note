package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NoteColorPalette
import com.example.data.NoteEntity
import com.example.markdown.parseMarkdownPreview
import com.example.ui.components.InfinitySymbol
import com.example.ui.theme.DeleteCoral
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.HeartRose
import com.example.ui.theme.PureBlackButton

@Composable
fun HomeScreen(
  notes: List<NoteEntity>,
  onAddNote: () -> Unit,
  onOpenNote: (NoteEntity) -> Unit,
  onToggleHeart: (NoteEntity) -> Unit,
  onDeleteNote: (NoteEntity) -> Unit,
  modifier: Modifier = Modifier,
) {
  var actionTargetNote by remember { mutableStateOf<NoteEntity?>(null) }
  val isDark = isSystemInDarkTheme()
  val haptics = LocalHapticFeedback.current

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.statusBars)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Minimal, clutter-free header
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      if (notes.isEmpty()) {
        EmptyNotesPlaceholder(modifier = Modifier.fillMaxSize().padding(bottom = 88.dp))
      } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
          val columnCount =
            when {
              maxWidth >= 840.dp -> 3
              maxWidth >= 340.dp -> 2
              else -> 1
            }

          LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            contentPadding =
              PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 112.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
          ) {
            items(items = notes, key = { it.id }) { note ->
              NotePreviewCard(
                note = note,
                isDark = isDark,
                onClick = { onOpenNote(note) },
                onLongPress = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  actionTargetNote = note
                },
              )
            }
          }
        }
      }
    }

    // Simple purple plus in a black circle
    Surface(
      onClick = onAddNote,
      shape = CircleShape,
      color = PureBlackButton,
      border =
        BorderStroke(
          width = 1.5.dp,
          color = if (isDark) Color(0xFF2D2540) else Color(0xFF1E1E24),
        ),
      shadowElevation = 10.dp,
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .navigationBarsPadding()
          .padding(end = 22.dp, bottom = 22.dp)
          .size(62.dp)
          .minimumInteractiveComponentSize()
          .testTag("add_note_button"),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = stringResource(R.string.cd_add_note),
          tint = ElectricPurple,
          modifier = Modifier.size(30.dp),
        )
      }
    }

    // Long-press action overlay (Heart + Delete)
    AnimatedVisibility(
      visible = actionTargetNote != null,
      enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
      exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
    ) {
      val target = actionTargetNote
      if (target != null) {
        NoteLongPressOverlay(
          note = target,
          isDark = isDark,
          onDismiss = { actionTargetNote = null },
          onHeartClick = {
            onToggleHeart(target)
            actionTargetNote = null
          },
          onDeleteClick = {
            onDeleteNote(target)
            actionTargetNote = null
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotePreviewCard(
  note: NoteEntity,
  isDark: Boolean,
  onClick: () -> Unit,
  onLongPress: () -> Unit,
) {
  val palette = NoteColorPalette.forIndex(note.colorIndex)
  val cardBg = palette.background(isDark)
  val cardBorder = palette.border(isDark)
  val accentColor = MaterialTheme.colorScheme.primary
  val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
  val codeBg = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.12f else 0.07f)

  val previewText =
    remember(note.content, isDark, accentColor, mutedColor) {
      parseMarkdownPreview(
        markdown = note.content,
        accentColor = accentColor,
        codeBackground = codeBg,
        mutedColor = mutedColor,
      )
    }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = cardBg,
    border = BorderStroke(1.dp, cardBorder),
    tonalElevation = 1.dp,
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .combinedClickable(onClick = onClick, onLongClick = onLongPress)
        .testTag("note_card_${note.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      if (note.title.isNotBlank() || note.isHearted) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          if (note.title.isNotBlank()) {
            Text(
              text = note.title,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }

          if (note.isHearted) {
            Icon(
              imageVector = Icons.Rounded.Favorite,
              contentDescription = stringResource(R.string.cd_hearted),
              tint = HeartRose,
              modifier = Modifier.padding(start = 8.dp, top = 2.dp).size(16.dp),
            )
          }
        }
      }

      if (previewText.isNotEmpty()) {
        if (note.title.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
          text = previewText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
          maxLines = 6,
          overflow = TextOverflow.Ellipsis,
        )
      } else if (note.title.isBlank()) {
        Text(
          text = stringResource(R.string.untitled_note),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun EmptyNotesPlaceholder(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(horizontal = 32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier.size(72.dp)
          .clip(CircleShape)
          .background(ElectricPurple.copy(alpha = 0.14f)),
      contentAlignment = Alignment.Center,
    ) {
      InfinitySymbol(
        color = ElectricPurple,
        modifier = Modifier.size(38.dp),
      )
    }
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = stringResource(R.string.empty_notes_title),
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.empty_notes_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun NoteLongPressOverlay(
  note: NoteEntity,
  isDark: Boolean,
  onDismiss: () -> Unit,
  onHeartClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  val palette = NoteColorPalette.forIndex(note.colorIndex)

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color.Black.copy(alpha = 0.56f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        )
        .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, palette.border(isDark)),
      shadowElevation = 16.dp,
      modifier =
        Modifier.widthIn(max = 360.dp)
          .fillMaxWidth()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
          ),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text =
            note.title.ifBlank {
              note.content.lineSequence().firstOrNull()?.take(40)
                ?: stringResource(R.string.untitled_note)
            },
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            onClick = onHeartClick,
            shape = RoundedCornerShape(16.dp),
            color = HeartRose.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.35f)),
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("long_press_heart_button"),
          ) {
            Row(
              modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector =
                  if (note.isHearted) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                contentDescription = null,
                tint = HeartRose,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text =
                  stringResource(
                    if (note.isHearted) R.string.action_unheart else R.string.action_heart
                  ),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = HeartRose,
              )
            }
          }

          Surface(
            onClick = onDeleteClick,
            shape = RoundedCornerShape(16.dp),
            color = DeleteCoral.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, DeleteCoral.copy(alpha = 0.35f)),
            modifier =
              Modifier.weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("long_press_delete_button"),
          ) {
            Row(
              modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                tint = DeleteCoral,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = stringResource(R.string.action_delete),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = DeleteCoral,
              )
            }
          }
        }
      }
    }
  }
}
