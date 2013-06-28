package com.beauty.android.reader;

import java.util.ArrayList;
import java.util.List;

import com.beauty.android.reader.setting.SettingManager;
import com.beauty.android.reader.view.Book;
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
        mBookList = new ArrayList<Book>();
        Book book0 = new Book(0, "三国演义", "罗贯中", R.drawable.sanguoyanyi,
                "滚滚长江东逝水，浪花淘尽英雄。是非成败转头空。青山依旧在，几度夕阳红。白发渔樵江渚上，惯看秋月春风。一壶浊酒喜相逢。古今多少事，都付笑谈中。", "sanguoyanyi.txt", "GBK");
        mBookList.add(book0);
        Book book1 = new Book(1, "红楼梦", "曹雪芹", R.drawable.hongloumeng,
                "花谢花飞飞满天，红消香断有谁怜？游丝软系飘春榭，落絮轻沾扑绣帘。天尽头，何处有香丘？未若锦囊收艳骨，一抔净土掩风流。质本洁来还洁去，强于污淖陷渠沟。一年三百六十日，风刀霜剑严相逼。",
                "hongloumeng.txt", "GBK");
        mBookList.add(book1);
        Book book2 = new Book(
                2,
                "水浒传",
                "施耐庵",
                R.drawable.shuihuzhuan,
                "天魁星·呼保义·宋江 天罡星·玉麒麟·卢俊义 天机星·智多星·吴用 天闲星·入云龙·公孙胜 天勇星·大刀·关胜 天雄星·豹子头·林冲 天猛星·霹雳火·秦明 天威星·双鞭将·呼延灼 天英星·小李广·花荣 天贵星·小旋风·柴进 天富星·扑天雕·李应 天满星·美髯公·朱仝 天孤星·花和尚·鲁智深 天伤星·行者·武松 天立星·双枪将·董平 天捷星·没羽箭·张清 天暗星·青面兽·杨志 天佑星·金枪手·徐宁",
                "shuihuzhuan.txt", "GBK");
        mBookList.add(book2);
        Book book3 = new Book(
                3,
                "西游记",
                "吴承恩",
                R.drawable.xiyouji,
                "《西游记》是一部中国古典神魔小说，为中国“四大名著”之一。成书于16世纪明朝中叶，是最优秀的神话小说，也是一部群众创作和文人创作相结合的作品。《西游记》主要描写的是孙悟空保唐僧西天取经，历经九九八十一难的故事。",
                "xiyouji.txt", "GBK");
        mBookList.add(book3);
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
