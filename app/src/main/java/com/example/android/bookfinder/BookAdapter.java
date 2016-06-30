package com.example.android.bookfinder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An adapter subclass to insert Book objects into ListViews.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        Book currentBook = getItem(position);

        TextView title = (TextView) listItemView.findViewById(R.id.title_text_view);
        TextView author = (TextView) listItemView.findViewById(R.id.author_text_view);

        title.setText(currentBook.getTitle());
        author.setText(currentBook.getAuthor());

        return listItemView;
    }
}
