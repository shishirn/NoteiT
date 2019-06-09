package com.spacetech.noteit.com.spacetech.noteid.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

   @Insert
    void insertNote(Note note);

   @Delete
    void deleteNote(Note note);

   //Need to verify how this is implemented ???
   @Update
   void updateNote(Note note);

   @Query("select * from note where noteid = :noteId ")
    Note getNote(int noteId);

   @Query("select * from note")
   List<Note> getAllNotes();
}
