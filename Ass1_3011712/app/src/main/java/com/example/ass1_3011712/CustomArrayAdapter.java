package com.example.ass1_3011712;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomArrayAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BookDetails> books;

    public CustomArrayAdapter(Context c, ArrayList<BookDetails> books){
        context = c;
        this.books = books;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // view holder to save us from requesting references to items over and over again
        ViewHolder holder;
        // if we do not have a view to recycle then inflate the layout and fix up the view holder
        if (convertView == null) {
            holder = new ViewHolder();
            // get access to the layout infaltor service
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // inflate the XML custom item layout into a view to which we can add data
            convertView = inflater.inflate(R.layout.list_book, parent, false);
            // pull all the items from the XML so we can modify them
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
            holder.tv_genre = (TextView) convertView.findViewById(R.id.tv_genre);
            holder.tv_status1 = (TextView) convertView.findViewById(R.id.tv_status1);
            holder.tv_status2 = (TextView) convertView.findViewById(R.id.tv_status2);
            // set the view holder as a tag on this convert view in case it needs to be recycled
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // set all the data on the fields before returning it
        holder.tv_title.setText(books.get(position).getTitle());
        holder.tv_author.setText(books.get(position).getAuthor());
        holder.tv_genre.setText(books.get(position).getGenre());

        // check which activity owning this custom array adapter
        if (context.getClass().getName().equals("com.example.ass1_3011712.BooksActivity")){
            holder.tv_status2.setText("Available stock: ");
            holder.tv_status2.setText("" + books.get(position).getCheckedIn());
        }
        else{
            String isbn = books.get(position).getIsbnS().get(0).getIsbn();
            holder.tv_status1.setText("ISBN: ");
            holder.tv_status2.setText(isbn);
        }

        // return the constructed view
        return convertView;

    }

    static class ViewHolder{
        public TextView tv_title;
        public TextView tv_author;
        public TextView tv_genre;
        public TextView tv_status1;
        public TextView tv_status2;
    }
}
