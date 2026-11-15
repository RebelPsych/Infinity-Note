package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.HomeScreen
import com.example.ui.InfinityNoteViewModel
import com.example.ui.NoteEditorScreen
import com.example.ui.theme.InfinityNoteTheme

class MainActivity : ComponentActivity() {

  private val viewModel: InfinityNoteViewModel by viewModels {
    val db = AppDatabase.getInstance(applicationContext)
    InfinityNoteViewModel.factory(NoteRepository(db.noteDao()))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      InfinityNoteTheme {
        val notes by viewModel.notes.collectAsStateWithLifecycle()
        val editorState by viewModel.editorState.collectAsStateWithLifecycle()
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
              viewModel.flushSave()
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        AnimatedContent(
          targetState = editorState.isOpen,
          transitionSpec = {
            (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
              scaleIn(
                initialScale = 0.98f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              )) togetherWith fadeOut(spring(stiffness = Spring.StiffnessMedium))
          },
          label = "screen_transition",
        ) { isEditorOpen ->
          if (isEditorOpen) {
            NoteEditorScreen(
              editorState = editorState,
              onTitleChanged = viewModel::onTitleChanged,
              onContentChanged = viewModel::onContentChanged,
              onColorSelected = viewModel::onColorSelected,
              onUndo = viewModel::undo,
              onRedo = viewModel::redo,
              onBack = viewModel::closeEditor,
            )
          } else {
            HomeScreen(
              notes = notes,
              onAddNote = viewModel::openNewNote,
              onOpenNote = viewModel::openNote,
              onToggleHeart = viewModel::toggleHeart,
              onDeleteNote = viewModel::deleteNote,
            )
          }
        }
      }
    }
  }
}
