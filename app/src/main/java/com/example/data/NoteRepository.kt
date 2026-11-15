package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
  val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

  suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

  suspend fun saveNote(note: NoteEntity): Long {
    return if (note.id == 0L) {
      noteDao.insertNote(note)
    } else {
      noteDao.updateNote(note)
      note.id
    }
  }

  suspend fun setHearted(id: Long, hearted: Boolean) = noteDao.setHearted(id, hearted)

  suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
}
