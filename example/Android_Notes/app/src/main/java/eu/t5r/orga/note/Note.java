package eu.t5r.orga.note;

import java.util.UUID;

/**
 * Created by Tobias Reichert
 */
public class Note {
    String id;
    String title;
    String text;
    long changeTime = 0;
    long syncTime = 0;

    public Note() {
        this.id = UUID.randomUUID().toString();
        this.title = new String();
        this.text = new String();
        makeChange();
    }

    public Note(String title, String text) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.title = title;
        makeChange();
    }

    public Note(String id, String title, String text) {
        this.id = id;
        this.text = text;
        this.title = title;
        makeChange();
    }

    //SETTER
    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    //MAKER
    public void makeChange() {
        changeTime = System.currentTimeMillis();
    }

    public void makeSync() {
        syncTime = System.currentTimeMillis();
    }

    //GETTER
    public long getChangeTime() {
        return changeTime;
    }

    public long getSyncTime() {
        return syncTime;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public String getXML() {
        String result = "<note><id>" + id + "</id><title>" + title + "</title><text>" + text + "</text><change>" + changeTime + "</change><sync>" + syncTime + "</sync></note>";
        return result;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", changeTime=" + changeTime +
                ", syncTime=" + syncTime +
                '}';
    }
}
