package com.spacetech.noteit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spacetech.noteit.com.spacetech.noteid.db.Note;
import com.spacetech.noteit.com.spacetech.noteid.db.NoteitDatabase;

import java.util.ArrayList;
import java.util.List;

public class ShowNotesListActivity extends AppCompatActivity {

    RecyclerView mNotesRecyclerView;
    NoteitDatabase mDb;
    private NotesAdapter mNotesAdapter;
    FloatingActionButton mNewNoteFab;
    Toolbar mToolBar;
    SharedPreferences mSharedPref;
    Menu mMenu;
    boolean mIsGrid;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notes_list);

        //Get shared preference for this app
        mSharedPref = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        //Getting shared preference value for list/grid layout
        mIsGrid = mSharedPref.getBoolean(getString(R.string.isGrid), false);

        mNotesRecyclerView = findViewById(R.id.notesRecyclerView);
        mNewNoteFab = findViewById(R.id.newNoteFab);
        mToolBar = findViewById(R.id.toolbar3);

        setSupportActionBar(mToolBar);
        mNewNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowNotesListActivity.this, AddNoteActivity.class);
                intent.putExtra(AppConstants.INTENT_ACTION, AppConstants.INSERT);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_INSERT);
            }
        });

        // get the DB reference into mDb variable
        mDb = Room.databaseBuilder(getApplicationContext(),
                NoteitDatabase.class, "noteitdb").build();
        ShowNotesListAsyncTask showNotesListAsyncTask = new ShowNotesListAsyncTask();
        showNotesListAsyncTask.execute();


        //Adding Layout manager based on preference and setting it for the recycler view
        RecyclerView.LayoutManager layoutManager;
        if (mIsGrid) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new LinearLayoutManager(this);
        }

        mNotesAdapter = new NotesAdapter(new ArrayList<Note>());
        mNotesRecyclerView.setAdapter(mNotesAdapter);
        mNotesRecyclerView.setLayoutManager(layoutManager);

        //Adding Swipe and delete feature
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(mNotesAdapter, this));
        itemTouchHelper.attachToRecyclerView(mNotesRecyclerView);
    }

    // Logic for menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.notes_list_actions, menu);
        mMenu = menu;
        if (mIsGrid) {
            menu.getItem(0).setIcon(R.drawable.ic_list_black_24dp);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_grid_on_black_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // get current preference for notes view - List or Grid

        if (!mIsGrid) {
            showGridView(this);
        } else {
            showListView(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGridView(Context context) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        mNotesRecyclerView.setLayoutManager(gridLayoutManager);

        //Updating shared preference key "isGrid" to 1.
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(getString(R.string.isGrid), true);
        mIsGrid = true;
        editor.apply();

        //updating the menu icon to show list view icon
        mMenu.getItem(0).setIcon(R.drawable.ic_list_black_24dp);
    }

    private void showListView(Context context) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mNotesRecyclerView.setLayoutManager(linearLayoutManager);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(getString(R.string.isGrid), false);
        mIsGrid = false;
        editor.apply();

        //updating the menu icon to show grid view icon
        mMenu.getItem(0).setIcon(R.drawable.ic_grid_on_black_24dp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_INSERT && resultCode == RESULT_OK) {
            List<Note> notes = mNotesAdapter.getNotes();
            notes.add((Note) data.getSerializableExtra("note"));
            mNotesAdapter.setNotes(notes);
            mNotesAdapter.notifyItemInserted(mNotesAdapter.getItemCount() + 1);
        }
        if (requestCode == AppConstants.REQUEST_CODE_UPDATE && resultCode == RESULT_OK) {
            int position = (int) data.getSerializableExtra(AppConstants.POSITION);
            Note note = (Note) data.getSerializableExtra("note");
            mNotesAdapter.notifyItemChanged(position, note);
        }
    }

    class ShowNotesListAsyncTask extends AsyncTask<Void, Void, List<Note>> {
        // getting all notes from DB
        @Override
        protected List<Note> doInBackground(Void... voids) {
            List<Note> notes = mDb.noteDao().getAllNotes();
            return notes;
        }

        //updating member variable mNotes with the returned value from DB getAllNotes()
        @Override
        protected void onPostExecute(List<Note> notes) {
            super.onPostExecute(notes);
            mNotesAdapter.setNotes(notes);
            mNotesAdapter.notifyDataSetChanged();
        }
    }

    //Deleting Note from DB on Swipe
    class DeleteNoteListAsyncTask extends AsyncTask<Note, Void, Void> {

        @Override
        protected Void doInBackground(Note... note) {
            mDb.noteDao().deleteNote(note[0]);
            return null;
        }
    }

    class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
        List<Note> notes;

        NotesAdapter(List<Note> notes) {
            this.notes = notes;
        }

        public List<Note> getNotes() {
            return notes;
        }

        public void setNotes(List<Note> notes) {
            this.notes = notes;
        }

        public void deleteNote(int position) {
            Note mRecentlyDeletedItem = notes.get(position);
            DeleteNoteListAsyncTask deleteNoteListAsyncTask = new DeleteNoteListAsyncTask();
            deleteNoteListAsyncTask.execute(mRecentlyDeletedItem);
            notes.remove(position);
            notifyItemRemoved(position);
        }

        @NonNull
        @Override
        public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // create a view object
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_item, parent, false);

            TextView notesTextView = v.findViewById(R.id.notesTextView);
            TextView titleTextView = v.findViewById(R.id.titleTextView);
            CardView cardView = v.findViewById(R.id.notesCardView);
            NotesViewHolder notesViewHolder = new NotesViewHolder(v, notesTextView, titleTextView, cardView);
            return notesViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull NotesViewHolder holder, final int position) {
            bindViewHolderHelper(holder, notes.get(position));
        }

        @Override
        public void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads == null || payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                bindViewHolderHelper(holder, (Note) payloads.get(0));
            }
        }

        private void bindViewHolderHelper(@NonNull final NotesViewHolder holder, final Note note) {

            holder.mTitleTextView.setText(note.getTitle());
            holder.mNotesTextView.setText(note.getNoteText());
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowNotesListActivity.this, AddNoteActivity.class);
                    //intent.putExtra("title", ((TextView)v.findViewById(R.id.titleTextView)).getText().toString());
                    intent.putExtra(AppConstants.NOTE, note);
                    intent.putExtra(AppConstants.INTENT_ACTION, AppConstants.UPDATE);
                    intent.putExtra(AppConstants.POSITION, holder.getAdapterPosition());
                    //startActivity(intent);
                    startActivityForResult(intent, AppConstants.REQUEST_CODE_UPDATE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        class NotesViewHolder extends RecyclerView.ViewHolder {
            public View mItemView;
            public TextView mNotesTextView;
            public TextView mTitleTextView;
            public CardView mCardView;

            public NotesViewHolder(View mItemView, @NonNull TextView mNotesTextView, TextView mTitleTextView, CardView mCardView) {
                super(mItemView);
                this.mItemView = mItemView;
                this.mNotesTextView = mNotesTextView;
                this.mTitleTextView = mTitleTextView;
                this.mCardView = mCardView;
            }
        }

    }
}
