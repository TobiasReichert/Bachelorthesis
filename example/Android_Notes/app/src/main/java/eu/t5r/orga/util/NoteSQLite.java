package eu.t5r.orga.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import eu.t5r.orga.note.Note;

/**
 * Created by tobias on 27.09.17.
 */

public class NoteSQLite extends MDBSAndroidSQLite {

    private static final String NOTE_TABLE_NAME = "Note";
    private static final String NOTE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS '"
            + NOTE_TABLE_NAME
            + "' ("
            + "id TEXT NOT NULL PRIMARY KEY, "
            + "synctime INTEGER, "
            + "title TEXT, "
            + "text TEXT "
            + ");";



    public NoteSQLite(Context context) {
        super(context);
        SQLiteDatabase db = this.getWritableDatabase(); //for onCreate call
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    public void createNotes(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(NOTE_TABLE_SQL);
        db.close();
    }

    public void setNotes(List<Note> notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Note note:notes){
            ContentValues values = new ContentValues();
            values.put("id", note.getId());
            values.put("synctime", note.getSyncTime());
            values.put("title", note.getTitle());
            values.put("text", note.getText());
            db.insert(NOTE_TABLE_NAME, null, values);
        }
    }

    public void clearNotes(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTE_TABLE_NAME, null,
                null);
    }

    public List<Note> getNotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                NOTE_TABLE_NAME,
                new String[]{"id", "synctime", "title", "text"},
                null,
                null,
                null,
                null,
                null,
                null);

        List<Note> notes = new ArrayList<>();

        while (cursor.moveToNext()) {
            Note n = new Note();
            n.setId(cursor.getString(0));
            n.setSyncTime(cursor.getLong(1));
            n.setTitle(cursor.getString(2));
            n.setText(cursor.getString(3));
            notes.add(n);
        }
        cursor.close();
        return notes;
    }

}
