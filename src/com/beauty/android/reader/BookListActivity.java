package com.beauty.android.reader;

import java.io.InputStream;
import java.util.List;

import net.youmi.android.AdManager;
import net.youmi.android.spot.SpotManager;

import com.beauty.android.reader.utils.XMLParse;
import com.beauty.android.reader.vo.Book;
import com.umeng.analytics.MobclickAgent;

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

        AdManager.getInstance(this).setEnableDebugLog(false);
        AdManager.getInstance(this).init("53b17049da69267e", "b5a2b42941de732a", false);
        // 预加载插播广告
        SpotManager.getInstance(this).loadSpotAds();

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
                overridePendingTransition(R.anim.activity_slide_in_from_right, R.anim.activity_slide_out_to_left);
            }

        });

    }

    private void loadData() {
        try {
            InputStream inputStream = getAssets().open("books.xml");
            mBookList = XMLParse.parseBookList(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobclickAgent.flush(this);
    }

}
