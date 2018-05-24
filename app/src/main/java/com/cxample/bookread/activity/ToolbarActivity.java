package com.cxample.bookread.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cxample.bookread.R;

/**
 * Created by yanqing on 2018/4/11.
 */

public class ToolbarActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_toolbar);
        initToolbar();
        FrameLayout layout = findViewById(R.id.container);
        LayoutInflater.from(this).inflate(layoutResID, layout);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(R.layout.activity_toolbar);
        initToolbar();
        FrameLayout layout = findViewById(R.id.container);
        layout.addView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(R.layout.activity_toolbar);
        initToolbar();
        FrameLayout layout = findViewById(R.id.container);
        layout.addView(view, params);
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }


    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
