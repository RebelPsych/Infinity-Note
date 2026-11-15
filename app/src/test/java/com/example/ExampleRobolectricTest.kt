package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.InfinityNoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: NoteRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    repository = NoteRepository(db.noteDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches Infinity Note`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Infinity Note", appName)
  }

  @Test
  fun `typing auto-saves to local storage and supports undo redo and heart delete`() = runBlocking {
    val viewModel = InfinityNoteViewModel(repository)
    viewModel.openNewNote()

    viewModel.onTitleChanged("Ideas")
    viewModel.onContentChanged("# First thought")
    viewModel.onContentChanged("# First thought\nHello world")
    viewModel.onColorSelected(1)

    delay(250)
    val saved = repository.allNotes.first()
    assertEquals(1, saved.size)
    assertEquals("Ideas", saved[0].title)
    assertEquals(1, saved[0].colorIndex)

    assertTrue(viewModel.editorState.value.canUndo)
    viewModel.undo()
    assertEquals("# First thought", viewModel.editorState.value.content)
    assertTrue(viewModel.editorState.value.canRedo)
    viewModel.redo()
    assertEquals("# First thought\nHello world", viewModel.editorState.value.content)

    viewModel.toggleHeart(saved[0])
    delay(200)
    assertTrue(repository.allNotes.first().first().isHearted)

    viewModel.deleteNote(saved[0])
    delay(200)
    assertFalse(repository.allNotes.first().isNotEmpty())
  }
}
