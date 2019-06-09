package com.spacetech.noteit.com.spacetech.noteid.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {Note.class}, version  =1)
public abstract class NoteitDatabase  extends RoomDatabase {
    public abstract NoteDao noteDao();
}
