package eu.t5r.orga.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import eu.t5r.orga.R;

/**
 * Created by tobias on 26.08.15.
 */
public class NoteAdapter extends ArrayAdapter<Note>{
    public NoteAdapter(Context context, int resource, List<Note> noteList){
        //super(context,R.layout.note_row);
        super(context,resource,noteList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View noteView = convertView;

        if (noteView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            noteView = vi.inflate(R.layout.note_row, null);
        }

        Note note = getItem(position);

        if (note != null) {
            String title = note.getTitle();
            String text = note.getText();

            TextView titleView = (TextView) noteView.findViewById(R.id.noteTitle);
            TextView textView = (TextView) noteView.findViewById(R.id.noteText);

            titleView.setText(title);
            textView.setText(text);
        }
        /*
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View noteView = inflater.inflate(R.layout.note_row, parent, false);

        String title = noteList.get(position).getTitle();
        String text = noteList.get(position).getText();
        Log.i("[abc]", "pos: " + position);
        Log.i("[abc]", "title: " + noteList.get(position).getTitle());

        TextView titleView = (TextView) noteView.findViewById(R.id.noteTitle);
        TextView textView = (TextView) noteView.findViewById(R.id.noteText);

        titleView.setText(title);
        textView.setText(text);*/

        return noteView;

    }
}
