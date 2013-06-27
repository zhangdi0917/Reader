package com.beauty.android.reader;

import java.util.ArrayList;
import java.util.List;

import com.beauty.android.reader.setting.SettingManager;
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

        SettingManager.getInstance().init(getApplicationContext());

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
        Book book0 = new Book(0, "三国演义", "罗贯中", -1,
                "滚滚长江东逝水，浪花淘尽英雄。是非成败转头空。青山依旧在，几度夕阳红。白发渔樵江渚上，惯看秋月春风。一壶浊酒喜相逢。古今多少事，都付笑谈中。", "sanguoyanyi.txt", "GBK");
        mBookList.add(book0);
        Book book1 = new Book(1, "红楼梦", "曹雪芹", -1,
                "花谢花飞飞满天，红消香断有谁怜？游丝软系飘春榭，落絮轻沾扑绣帘。天尽头，何处有香丘？未若锦囊收艳骨，一抔净土掩风流。质本洁来还洁去，强于污淖陷渠沟。一年三百六十日，风刀霜剑严相逼。",
                "hongloumeng.txt", "GBK");
        mBookList.add(book1);
    }

}
