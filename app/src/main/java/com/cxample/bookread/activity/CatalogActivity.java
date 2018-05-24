package com.cxample.bookread.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cxample.bookread.R;
import com.cxample.bookread.adapter.CatalogAdapter;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.BookDataBase;
import com.cxample.bookread.db.Episode;
import com.cxample.bookread.db.EpisodeDataBase;

import java.util.List;

public class CatalogActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ImageView mBackView;
    private CatalogAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        Intent intent = getIntent();
        if(intent == null) {
            finish();
            return;
        }
        int id = intent.getIntExtra("id", -1);
        if(id == -1) {
            finish();
            return;
        }
        initView();
        getEpisodes(id);
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.catalog_list);
        mBackView = findViewById(R.id.back);
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new CatalogAdapter(this);
        mAdapter.setItemClickListener(mItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getEpisodes(int bookId) {
        Book book = BookDataBase.getInstance(this).bookDao().getBook(bookId);
        if(book != null) {
            List<Episode> episodes = EpisodeDataBase.getInstance(this).episodeDao().getEpisodesByBookId(book.id);
            mAdapter.setCurrentEpisode(book.episode);
            mAdapter.setEpisodes(episodes);
            mRecyclerView.scrollToPosition(book.episode - 1);
        }
    }

    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Episode episode = (Episode)v.getTag();
            Intent intent = new Intent();
            intent.putExtra("episode", episode.episode_index);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
