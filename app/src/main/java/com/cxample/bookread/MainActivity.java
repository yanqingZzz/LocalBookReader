package com.cxample.bookread;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cxample.bookread.activity.FileBrowserActivity;
import com.cxample.bookread.activity.ReadActivity;
import com.cxample.bookread.adapter.BookListAdapter;
import com.cxample.bookread.constant.BoradcastAction;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.BookDataBase;
import com.cxample.bookread.utils.LoadLocalBookUtils;

import java.util.List;

/**
 * Created by yanqing on 2018/4/19.
 */

public class MainActivity extends AppCompatActivity {
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private RelativeLayout mRecordLayout;
    private TextView mBookTitleView;
    private TextView mBookAuthorView;
    private TextView mBookRecordView;
    private TextView mBookNoRecordView;
    private RecyclerView mRecyclerView;
    private BookListAdapter mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BoradcastAction.BOOK_RECORD_CHANGED.equals(action)) {
                getBookInfo();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");
        initToolbar();
        initView();
        getBookInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerLocalBroadcast();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterLocalBroadcast();
    }

    private void registerLocalBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BoradcastAction.BOOK_RECORD_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    private void unregisterLocalBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initView() {
        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_layout);
        mRecordLayout = findViewById(R.id.record_layout);
        mBookTitleView = findViewById(R.id.book_title);
        mBookAuthorView = findViewById(R.id.book_author);
        mBookRecordView = findViewById(R.id.book_read_record);
        mBookNoRecordView = findViewById(R.id.no_read_record);
        mRecyclerView = findViewById(R.id.recycler);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new BookListAdapter(this);
        mAdapter.setItemClickListener(mItemClickListener);
        mAdapter.setAddClickListener(mAddClickListener);
        mRecyclerView.setAdapter(mAdapter);

        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mAppBarLayout.addOnOffsetChangedListener(mOffsetChangedListener);
    }

    private boolean mIsTitleShow = false;
    private AppBarLayout.OnOffsetChangedListener mOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            int maxHeight = mCollapsingToolbarLayout.getHeight() - mToolbar.getHeight() - 20;
            if(Math.abs(verticalOffset) >= maxHeight && !mIsTitleShow) {
                mIsTitleShow = true;
                mCollapsingToolbarLayout.setTitle(getResources().getString(R.string.app_name));
            } else if(Math.abs(verticalOffset) < maxHeight && mIsTitleShow) {
                mIsTitleShow = false;
                mCollapsingToolbarLayout.setTitle("");
            }
        }
    };

    private void showBookRecord(Book book) {
        if(book != null) {
            mBookNoRecordView.setVisibility(View.GONE);
            mBookTitleView.setVisibility(View.VISIBLE);
            mBookAuthorView.setVisibility(View.VISIBLE);
            mBookRecordView.setVisibility(View.VISIBLE);

            mBookTitleView.setText(book.name);
            if(!TextUtils.isEmpty(book.author)) {
                mBookAuthorView.setText("作者：" + book.author);
            } else {
                mBookAuthorView.setText("作者：未知");
            }
            mBookRecordView.setText("已阅读至 " + book.episode + "/" + book.episode_count);

            mRecordLayout.setTag(book);
            mRecordLayout.setOnClickListener(mItemClickListener);
        } else {
            mBookNoRecordView.setVisibility(View.VISIBLE);
            mBookTitleView.setVisibility(View.GONE);
            mBookAuthorView.setVisibility(View.GONE);
            mBookRecordView.setVisibility(View.GONE);

            mRecordLayout.setOnClickListener(null);
        }
    }


    private void getBookInfo() {
        List<Book> books = BookDataBase.getInstance(this).bookDao().getAll();
        if(books != null && books.size() > 0) {
            showBookRecord(books.get(0));
        } else {
            showBookRecord(null);
        }
        mAdapter.setBooks(books);
    }

    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Book book = (Book)v.getTag();
            Intent intent = new Intent(MainActivity.this, ReadActivity.class);
            intent.putExtra("id", book.id);
            startActivity(intent);
        }
    };

    private View.OnClickListener mAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FILE_BROWSER);
        }
    };


    private static final int REQUEST_CODE_FILE_BROWSER = 100;
    private static final int REQUEST_CODE_READ = 101;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FILE_BROWSER) {
            if(resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                LoadLocalBookUtils.loadBook(MainActivity.this, path, mLoadLocalBookListener);
            }
        }
    }

    private ProgressDialog mProgressDialog;
    private LoadLocalBookUtils.OnLoadLocalBookListener mLoadLocalBookListener = new LoadLocalBookUtils.OnLoadLocalBookListener() {
        @Override
        public void onStart() {
            if(mProgressDialog == null || !mProgressDialog.isShowing()) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("正在加载...");
                mProgressDialog.show();
            }
        }

        @Override
        public void onEnd() {
            if(mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
                getBookInfo();
            }
        }

        @Override
        public void onError() {
            if(mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定退出阅读？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }
}
