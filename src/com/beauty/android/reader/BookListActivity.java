package com.beauty.android.reader;

import java.util.ArrayList;
import java.util.List;

import com.beauty.android.reader.view.Book;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

public class BookListActivity extends Activity {

    private ListView mListView;

    private BookListAdapter mBookListAdapter;

    private List<Book> mBookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_book_list);

        loadData();

        mListView = (ListView) findViewById(R.id.listview);
        mBookListAdapter = new BookListAdapter(this, mBookList);
        mListView.setAdapter(mBookListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = mBookList.get(position);
                Intent intent = new Intent(BookListActivity.this, ReadActivity.class);
                intent.putExtra(ReadActivity.INTENT_EXTRA_BOOK, book);
                startActivity(intent);
            }

        });
    }

    private void loadData() {
        mBookList = new ArrayList<Book>();
        Book book0 = new Book(0, "笑傲江湖", "金庸", -1, "江湖笑，恩怨了", "book0.txt", "GBK");
        mBookList.add(book0);
        Book book1 = new Book(1, "倚天神雕", "金庸", -1, "一帮美女", "book1.txt", "GBK");
        mBookList.add(book1);
    }

}
