package eu.t5r.orga.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import eu.t5r.MDBS.MDBSConnection;
import eu.t5r.orga.note.Note;
import eu.t5r.orga.note.NoteXMLParser;

public class ServiceA extends Service {
    private final IBinder mBinder = new LocalBinder();

    private NoteSQLite sql;
    public MDBSConnection con;
    List<Note> notes;

    public void setNotes(List<Note> notes) {
        this.notes = notes;

        sql.clearNotes();
        sql.setNotes(notes);
        try {
            con.sync();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<Note> getNotes() {
        this.notes.clear();

        List<Note> n = sql.getNotes();
        System.out.println(n);
        this.notes.addAll(n);

        return this.notes;
    }

    public class LocalBinder extends Binder {
        public ServiceA getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServiceA.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        System.out.println("Create");
        //Thread t = new Thread(new OpenThread());
        //t.start();
        //sc = new SocketClient("10.0.2.1", 5000);
        //NoteActivity.IncomingHandler h = new NoteActivity.IncomingHandler();
        notes = new ArrayList<>();

        sql = new NoteSQLite(this);
        sql.createNotes();
        sql.clearNotes();
        try {
            con = new MDBSConnection(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.setHost("10.156.4.46");
        Thread t = new Thread(new SaveThread());
        t.start();


    }
    class SaveThread implements Runnable {
        @Override
        public void run() {
            try {
                con.connect();
                try {
                    con.addSyncTable("Note", 1);
                } catch (Exception e) {
                }
                System.out.println(">>" + con);
                con.logIn("test", "test");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        //TODO Socket
    }

    public void saveData() {
        try {
            con.sync();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
/*
    class SaveThread implements Runnable {
        @Override
        public void run() {
            String filename = "note.xml";
            StringBuffer s = new StringBuffer();
            FileOutputStream outputStream;

            s.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            s.append(makeNoteXML());

            Log.i("NA_Save", s.toString());
            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(s.toString().getBytes());
                outputStream.close();
                //sc.send(s.toString());
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(this, "save error", Toast.LENGTH_SHORT).show(); TODO
            }
        }
    }

    class OpenThread implements Runnable {
        public void run() {
            FileInputStream fos;
            try {
                fos = openFileInput("note.xml");
                NoteXMLParser nxp = new NoteXMLParser();
                SAXParserFactory.newInstance()
                        .newSAXParser().parse(fos, nxp);

            //for (Note note:nxp.getNotes()){
            //    Log.i("NA_Open",note.getXML());
            //}

                notes = nxp.getNotes();
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(this, "open error", Toast.LENGTH_SHORT).show(); TODO
            }
            if (notes == null) {
                notes = new ArrayList<>();
                notes.add(new Note("Erste Notiz", "Hier könnte ihr text stehen"));
                saveData();
            }
            System.out.println("Scaned XML");
        }
    }
    */

}
