package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY isHearted DESC, updatedAt DESC")
  fun getAllNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
  suspend fun getNoteById(id: Long): NoteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity): Long

  @Update
  suspend fun updateNote(note: NoteEntity)

  @Query("UPDATE notes SET isHearted = :hearted WHERE id = :id")
  suspend fun setHearted(id: Long, hearted: Boolean)

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deleteNoteById(id: Long)
}
