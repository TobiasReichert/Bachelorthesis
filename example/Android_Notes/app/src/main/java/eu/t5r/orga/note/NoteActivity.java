package eu.t5r.orga.note;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;

import eu.t5r.MDBS.MDBSConnection;
import eu.t5r.MDBS.structs.SyncListener;
import eu.t5r.orga.R;
import eu.t5r.orga.util.BooleanContainer;
import eu.t5r.orga.util.MDBSAndroidSQLite;
import eu.t5r.orga.util.ServiceA;

import static eu.t5r.orga.R.id.noteListView;

public class NoteActivity extends AppCompatActivity implements SyncListener {

    //RequestCodes
    int rcNew = 1234;
    int rcExist = 1235;
    //Service
    ServiceA mService;
    boolean mBound = false;

    ListView lv1;
    //List<Note> noteList;
    ArrayAdapter<Note> adapter;
    NoteActivity that;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent intent = new Intent(this, ServiceA.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        that = this;
        final BooleanContainer onCLick = new BooleanContainer();
        onCLick.setB(false);
        lv1 = (ListView) findViewById(noteListView);

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!onCLick.getB()) {
                    Intent in = new Intent(that, NoteEditActivity.class);
                    Note note = (Note) adapterView.getItemAtPosition(position);
                    in.putExtra("noteId", note.getId());
                    in.putExtra("noteText", note.getText());
                    in.putExtra("noteTitle", note.getTitle());
                    startActivityForResult(in, rcExist);
                }
            }
        });

        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                onCLick.setB(true);
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(NoteActivity.this, lv1);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_note, popup.getMenu());

                final int pos = position;
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        Toast.makeText(NoteActivity.this, "Delete: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        List<Note> n = mService.getNotes();
                        System.out.println("befor del:" + n);
                        System.out.println(n.get(pos));
                        n.remove(pos);
                        System.out.println("befor del:" + n);
                        mService.setNotes(n);
                        adapter.notifyDataSetChanged();
                        onCLick.setB(false);
                        return true;
                    }
                });
                popup.show();//showing popup menu
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void syncPerformed(int i, boolean b) {
        mService.getNotes();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                /*
                case MyService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getNotes().getString("str1");
                    textStrValue.setText("Str Message: " + str1);
                    break;
                    */
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServiceA.LocalBinder binder = (ServiceA.LocalBinder) service;
            mService = binder.getService();
            System.out.println("Service con");
            mBound = true;

            List<Note> a = mService.getNotes();
            System.out.println(a);
            adapter = new NoteAdapter(that, R.layout.note_row, a);
            //Log.i("[abc]", "blub");
            lv1.setAdapter(adapter);
            mService.con.addSyncListener(NoteActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<Note> n = mService.getNotes();
        if (resultCode == RESULT_OK) {
            if (requestCode == rcNew) {
                Note note = new Note();
                //note.setId(data.getStringExtra("noteId")); not necessary because new
                note.setText(data.getStringExtra("noteText"));
                note.setTitle(data.getStringExtra("noteTitle"));
                note.makeChange();

                n.add(note);
            } else if (requestCode == rcExist) {
                String id = data.getStringExtra("noteId");
                String title = data.getStringExtra("noteTitle");
                String text = data.getStringExtra("noteText");
                for (Note note : n) {
                    if (note.getId().equals(id)) {
                        note.setText(text);
                        note.setTitle(title);
                        note.makeChange();
                    }
                }
            }
            adapter.notifyDataSetChanged();
            mService.setNotes(n);
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenNote(View view) {
        Intent in = new Intent(this, NoteEditActivity.class);
        startActivityForResult(in, rcNew);
    }
}
