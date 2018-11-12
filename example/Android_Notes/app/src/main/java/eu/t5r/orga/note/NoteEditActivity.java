package eu.t5r.orga.note;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import eu.t5r.orga.R;

public class NoteEditActivity extends AppCompatActivity {
    EditText noteText;
    EditText noteTitle;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        noteText = (EditText) findViewById(R.id.editText);
        noteTitle = (EditText) findViewById(R.id.editText2);

        Bundle data = getIntent().getExtras();
        if(!(data == null || data.isEmpty())){
            String note = data.getString("noteText");
            noteText.setText(note);
            String title = data.getString("noteTitle");
            noteTitle.setText(title);
            id = data.getString("noteId");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        //MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.note_edit_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.saveNote:
                saveIntend();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        saveIntend();
        finish();
    }

    public void saveIntend(){
        Intent data = new Intent();
        data.putExtra("noteText", noteText.getText().toString());
        data.putExtra("noteTitle", noteTitle.getText().toString());
        data.putExtra("noteId", id);
        setResult(RESULT_OK, data);
    }
}
