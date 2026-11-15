package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditorSnapshot(
  val title: String,
  val content: String,
)

data class EditorUiState(
  val isOpen: Boolean = false,
  val sessionKey: Long = 0L,
  val noteId: Long? = null,
  val title: String = "",
  val content: String = "",
  val colorIndex: Int = 0,
  val isHearted: Boolean = false,
  val createdAt: Long = 0L,
  val canUndo: Boolean = false,
  val canRedo: Boolean = false,
  val historyVersion: Int = 0,
)

class InfinityNoteViewModel(private val repository: NoteRepository) : ViewModel() {

  val notes: StateFlow<List<NoteEntity>> =
    repository.allNotes.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptyList(),
    )

  private val _editorState = MutableStateFlow(EditorUiState())
  val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

  private val undoStack = ArrayDeque<EditorSnapshot>()
  private val redoStack = ArrayDeque<EditorSnapshot>()
  private var pendingUndoBase: EditorSnapshot? = null
  private var undoCommitJob: Job? = null
  private var saveJob: Job? = null
  private val saveMutex = Mutex()
  private var sessionCounter = 0L

  fun openNewNote() {
    flushSave()
    clearHistory()
    sessionCounter++
    _editorState.value =
      EditorUiState(
        isOpen = true,
        sessionKey = sessionCounter,
        noteId = null,
        title = "",
        content = "",
        colorIndex = 0,
        isHearted = false,
        createdAt = System.currentTimeMillis(),
      )
  }

  fun openNote(note: NoteEntity) {
    flushSave()
    clearHistory()
    sessionCounter++
    _editorState.value =
      EditorUiState(
        isOpen = true,
        sessionKey = sessionCounter,
        noteId = note.id,
        title = note.title,
        content = note.content,
        colorIndex = note.colorIndex,
        isHearted = note.isHearted,
        createdAt = note.createdAt,
      )
  }

  fun closeEditor() {
    commitPendingUndoSnapshot()
    flushSave()
    _editorState.update { it.copy(isOpen = false) }
  }

  fun onTitleChanged(newTitle: String) {
    val current = _editorState.value
    if (!current.isOpen || current.title == newTitle) return
    recordEditForUndo(
      previous = EditorSnapshot(current.title, current.content),
      next = EditorSnapshot(newTitle, current.content),
    )
    _editorState.update {
      it.copy(
        title = newTitle,
        canUndo = undoStack.isNotEmpty() || pendingUndoBase != null,
        canRedo = redoStack.isNotEmpty(),
      )
    }
    scheduleSave()
  }

  fun onContentChanged(newContent: String) {
    val current = _editorState.value
    if (!current.isOpen || current.content == newContent) return
    recordEditForUndo(
      previous = EditorSnapshot(current.title, current.content),
      next = EditorSnapshot(current.title, newContent),
    )
    _editorState.update {
      it.copy(
        content = newContent,
        canUndo = undoStack.isNotEmpty() || pendingUndoBase != null,
        canRedo = redoStack.isNotEmpty(),
      )
    }
    scheduleSave()
  }

  fun onColorSelected(colorIndex: Int) {
    val current = _editorState.value
    if (!current.isOpen || current.colorIndex == colorIndex) return
    _editorState.update { it.copy(colorIndex = colorIndex) }
    scheduleSave(immediate = true)
  }

  fun undo() {
    val current = _editorState.value
    if (!current.isOpen) return

    val target =
      if (pendingUndoBase != null) {
        undoCommitJob?.cancel()
        val base = pendingUndoBase!!
        pendingUndoBase = null
        base
      } else if (undoStack.isNotEmpty()) {
        undoStack.removeLast()
      } else {
        return
      }

    redoStack.addLast(EditorSnapshot(current.title, current.content))
    if (redoStack.size > MAX_HISTORY) {
      redoStack.removeFirst()
    }

    _editorState.update {
      it.copy(
        title = target.title,
        content = target.content,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty(),
        historyVersion = it.historyVersion + 1,
      )
    }
    scheduleSave(immediate = true)
  }

  fun redo() {
    val current = _editorState.value
    if (!current.isOpen || redoStack.isEmpty()) return

    commitPendingUndoSnapshot()
    val target = redoStack.removeLast()
    undoStack.addLast(EditorSnapshot(current.title, current.content))
    if (undoStack.size > MAX_HISTORY) {
      undoStack.removeFirst()
    }

    _editorState.update {
      it.copy(
        title = target.title,
        content = target.content,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty(),
        historyVersion = it.historyVersion + 1,
      )
    }
    scheduleSave(immediate = true)
  }

  fun toggleHeart(note: NoteEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.setHearted(note.id, !note.isHearted)
    }
  }

  fun deleteNote(note: NoteEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.deleteNoteById(note.id)
    }
  }

  fun flushSave() {
    saveJob?.cancel()
    val state = _editorState.value
    if (!state.isOpen) return
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { persistState(state) }
  }

  private fun scheduleSave(immediate: Boolean = false) {
    saveJob?.cancel()
    saveJob =
      viewModelScope.launch(Dispatchers.IO) {
        if (!immediate) {
          delay(80L)
        }
        persistState(_editorState.value)
      }
  }

  private suspend fun persistState(state: EditorUiState) {
    saveMutex.withLock {
      val latest = _editorState.value
      val target = if (latest.sessionKey == state.sessionKey) latest else state

      // Avoid cluttering storage with completely blank unsaved drafts
      if (
        target.noteId == null &&
          target.title.isBlank() &&
          target.content.isBlank() &&
          target.colorIndex == 0
      ) {
        return
      }

      val now = System.currentTimeMillis()
      val entity =
        NoteEntity(
          id = target.noteId ?: 0L,
          title = target.title,
          content = target.content,
          colorIndex = target.colorIndex,
          isHearted = target.isHearted,
          createdAt = if (target.createdAt > 0L) target.createdAt else now,
          updatedAt = now,
        )

      val savedId = repository.saveNote(entity)
      if (target.noteId == null && savedId > 0L) {
        _editorState.update { current ->
          if (current.sessionKey == target.sessionKey && current.noteId == null) {
            current.copy(noteId = savedId)
          } else {
            current
          }
        }
      }
    }
  }

  private fun recordEditForUndo(previous: EditorSnapshot, next: EditorSnapshot) {
    redoStack.clear()
    if (pendingUndoBase == null) {
      pendingUndoBase = previous
    }

    val delta =
      abs(next.content.length - previous.content.length) +
        abs(next.title.length - previous.title.length)
    val addedNewline = next.content.count { it == '\n' } != previous.content.count { it == '\n' }

    undoCommitJob?.cancel()
    if (delta > 3 || addedNewline) {
      commitPendingUndoSnapshot()
    } else {
      undoCommitJob =
        viewModelScope.launch {
          delay(350L)
          commitPendingUndoSnapshot()
          _editorState.update {
            it.copy(
              canUndo = undoStack.isNotEmpty(),
              canRedo = redoStack.isNotEmpty(),
            )
          }
        }
    }
  }

  private fun commitPendingUndoSnapshot() {
    undoCommitJob?.cancel()
    val base = pendingUndoBase ?: return
    pendingUndoBase = null
    if (undoStack.lastOrNull() != base) {
      undoStack.addLast(base)
      if (undoStack.size > MAX_HISTORY) {
        undoStack.removeFirst()
      }
    }
  }

  private fun clearHistory() {
    undoCommitJob?.cancel()
    pendingUndoBase = null
    undoStack.clear()
    redoStack.clear()
  }

  override fun onCleared() {
    flushSave()
    super.onCleared()
  }

  companion object {
    private const val MAX_HISTORY = 100

    fun factory(repository: NoteRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return InfinityNoteViewModel(repository) as T
        }
      }
  }
}
