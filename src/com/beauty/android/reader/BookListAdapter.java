package com.beauty.android.reader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.beauty.android.reader.vo.Book;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {

    private Context mContext;

    private List<Book> mBookList;

    private List<WeakReference<Bitmap>> mBookCovers = null;

    public BookListAdapter(Context context, List<Book> books) {
        mContext = context;
        mBookList = books;

        if (mBookList != null && mBookList.size() > 0) {
            mBookCovers = new ArrayList<WeakReference<Bitmap>>(books.size());
            for (Book book : books) {
                mBookCovers.add(new WeakReference<Bitmap>(book.getCoverBitmap(mContext)));
            }
        }
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

        Bitmap cover = mBookCovers.get(position).get();
        if (cover == null || cover.isRecycled()) {
            cover = mBookList.get(position).getCoverBitmap(mContext);
            mBookCovers.set(position, new WeakReference<Bitmap>(cover));
        }

        Book book = mBookList.get(position);

        holder.bookCover.setImageBitmap(cover);

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
