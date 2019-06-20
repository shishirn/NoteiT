package com.spacetech.noteit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.spacetech.noteit.com.spacetech.noteid.db.Note;
import com.spacetech.noteit.com.spacetech.noteid.db.NoteitDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        //Add action bar
        setSupportActionBar(mToolBar);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
        return true;
                //super.onOptionsItemSelected(item);
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
            return null;
        }

        private void setNoteAttributes(String noteText, String titleText) {
            mNote.setTitle(titleText);
            mNote.setNoteText(noteText);
            mNote.setTimestamp(System.currentTimeMillis());
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    false);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        }
    }
}


