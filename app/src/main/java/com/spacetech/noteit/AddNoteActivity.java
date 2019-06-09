package com.spacetech.noteit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

import com.spacetech.noteit.com.spacetech.noteid.db.Note;
import com.spacetech.noteit.com.spacetech.noteid.db.NoteitDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddNoteActivity extends AppCompatActivity {

   // public static final String NOTE = "note";
    TextView mTimeTextView;
    Toolbar mToolBar;
    Note mNote;
    EditText mTitleEditText;
    EditText mNoteEditText;
    NoteitDatabase mDb;
    public String mIntentAction;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy");
        Date date = new Date();
        mTimeTextView = findViewById(R.id.textView_Time);
        mTimeTextView.setText(df.format(date));
        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setTitle("NoteiT");
        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mTitleEditText = findViewById(R.id.editText_Title);
        mNoteEditText = findViewById(R.id.editText_Note);
        Log.d("@@@", "onCreate: ");
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = mNoteEditText.getText().toString();
                String titleText = mTitleEditText.getText().toString();
                if (!(TextUtils.isEmpty(noteText) && TextUtils.isEmpty(titleText))) {
                    Note note = new Note();
                    note.setTitle(titleText);
                    note.setNoteText(noteText);
                    note.setTimestamp(System.currentTimeMillis());
                    Intent data = new Intent();
                    data.putExtra("note", note);
                    Bundle noteDetailsBundle = getIntent().getExtras();
                    if (noteDetailsBundle != null) {
                        mIntentAction = noteDetailsBundle.getString(AppConstants.INTENT_ACTION);
                        if (mIntentAction.equals(AppConstants.UPDATE)) {
                            int position = noteDetailsBundle.getInt(AppConstants.POSITION);
                            data.putExtra(AppConstants.POSITION, position);
                        }
                    }
                    setResult(RESULT_OK, data);
                }

                finish();
            }
        });

        //Create-call DB
        mDb = Room.databaseBuilder(getApplicationContext(), NoteitDatabase.class, "noteitdb").build();

        //updating note when clicked from list
        Bundle noteDetailsBundle = getIntent().getExtras();
        if (noteDetailsBundle != null) {
            mIntentAction = noteDetailsBundle.getString(AppConstants.INTENT_ACTION);
            if (mIntentAction.equals(AppConstants.UPDATE)) {
                mNote = (Note) noteDetailsBundle.get(AppConstants.NOTE);
                mTitleEditText.setText(mNote.getTitle());
                mNoteEditText.setText(mNote.getNoteText());
            }
        }
    }


    @Override
    protected void onPause() {

        super.onPause();
        NoteDbAsyncTask noteDbAsyncTask = new NoteDbAsyncTask();
        noteDbAsyncTask.execute();
        Log.d("@@@", "onPause: ");


        // Toast.makeText(this, "on Pause",Toast.LENGTH_LONG);
    }

    class NoteDbAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String noteText = mNoteEditText.getText().toString();
            String titleText = mTitleEditText.getText().toString();

            if (!(TextUtils.isEmpty(noteText) && TextUtils.isEmpty(titleText))) {
                // Note note = new Note();

                if (mIntentAction.equals(AppConstants.INSERT)) {
                    mNote = new Note();
                    setNoteAttributes(noteText,titleText);
                    mDb.noteDao().insertNote(mNote);
                }
                else if (mIntentAction.equals(AppConstants.UPDATE)) {
                    setNoteAttributes(noteText,titleText);
                    mDb.noteDao().updateNote(mNote);
                }
            }


            // Below code snippet is to display all records in the table on console/log

           /* List<Note> notes = new ArrayList<>();
            notes = mDb.noteDao().getAllNotes();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (Note note : notes
            ) {
                Log.d("Title ###", ": " + note.getTitle());
                Log.d("Note ###", ": " + note.getNoteText());
                Date date = new Date(note.getTimestamp());
                Log.d("Timestamp ###", ": " + sdf.format(date));
                Log.d("NoteID ###", ": " + note.getNoteId());
            }*/
            return null;
        }

        private void setNoteAttributes(String noteText, String titleText) {
            mNote.setTitle(titleText);
            mNote.setNoteText(noteText);
            mNote.setTimestamp(System.currentTimeMillis());
        }
    }
}
