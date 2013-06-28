package com.beauty.android.reader;

import java.util.List;

import com.beauty.android.reader.view.Book;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Book> mBookList;

    public BookListAdapter(Context context, List<Book> books) {
        mContext = context;
        mBookList = books;
    }

    @Override
    public int getCount() {
        if (mBookList == null) {
            return 0;
        }
        return mBookList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.book_list_item, null);
            holder.bookCover = (ImageView) convertView.findViewById(R.id.book_cover);
            holder.bookName = (TextView) convertView.findViewById(R.id.book_name);
            holder.bookAuthor = (TextView) convertView.findViewById(R.id.book_author);
            holder.bookDescribe = (TextView) convertView.findViewById(R.id.book_describe);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Book book = mBookList.get(position);
        if (book.coverRes > 0) {
            holder.bookCover.setImageResource(book.coverRes);
        }
        holder.bookName.setText(book.name);
        holder.bookAuthor.setText(book.author);
        holder.bookDescribe.setText(book.description);
        
        return convertView;
    }

    static final class ViewHolder {
        ImageView bookCover;
        TextView bookName;
        TextView bookAuthor;
        TextView bookDescribe;
    }

}
