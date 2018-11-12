package eu.t5r.orga.note;
/**
 * @author tobias reichert
 */

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;


public class NoteXMLParser extends DefaultHandler {

    private final static Charset CHARSET = Charset.forName("utf8");

    private int takeNext = 0;
    private Note temp;
    private List<Note> notes;

    public List<Note> getNotes(){
        return notes;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "notes":
                notes = new LinkedList<>();
                break;
            case "note":
                temp = new Note();
                break;
            case "id":
                takeNext = 1;
                break;
            case "title":
                takeNext = 2;
                break;
            case "text":
                takeNext = 3;
                break;
            case "change":
                takeNext = 4;
                break;
            case "sync":
                takeNext = 5;
                break;

            default:
                break;
        }
    }

    //"<note><id>" + id + "</id><title>" + title + "</title><text>" + text + "</text><change>" + changeTime + "</change><sync>" + syncTime + "</sync></note>"
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "note":
                notes.add(temp);
                break;
            case "id":
            case "title":
            case "text":
            case "change":
            case "sync":
                takeNext = 0;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (takeNext){
            case 1:
                temp.setId(temp.getId() + new String(ch, start, length));
                break;
            case 2:
                temp.setTitle(temp.getTitle() + new String(ch, start, length));
                break;
            case 3:
                temp.setText(temp.getText() + new String(ch, start, length));
                break;
            case 4:
                temp.setChangeTime(Long.parseLong(new String(ch, start, length)));
                break;
            case 5:
                temp.setSyncTime(Long.parseLong(new String(ch, start, length)));
                break;
            default:
                break;
        }
    }
}