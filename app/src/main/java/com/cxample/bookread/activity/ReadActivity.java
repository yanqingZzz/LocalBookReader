package com.cxample.bookread.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cxample.bookread.R;
import com.cxample.bookread.bookview.BaseReadView;
import com.cxample.bookread.constant.BoradcastAction;
import com.cxample.bookread.constant.ReadConfig;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.BookDataBase;
import com.cxample.bookread.utils.SharePreferenceUtils;
import com.cxample.bookread.utils.Utils;

public class ReadActivity extends Activity implements OnReadViewListener {
    private RelativeLayout mTitleFunctionLayout;
    private LinearLayout mBottomFunctionLayout;
    private LinearLayout mSettingFunctionLayout;
    private ImageView mBackView;
    private TextView mEpisodeTitleView;
    private TextView mNextView;
    private TextView mPreviousView;
    private SeekBar mEpisodeProgress;
    private LinearLayout mCatalogBtn;
    private LinearLayout mSettingBtn;
    private LinearLayout mNightBtn;
    private ImageView mNightIconView;
    private TextView mNightTitleView;
    private SeekBar mScreenLightView;
    private TextView mSystemLightView;
    private TextView mTextSizeView;
    private ImageView mCurrentBgView;

    private BaseReadView mBookReadView;
    private Book mBook;

    private Animation mAnimationTopIn;
    private Animation mAnimationTopOut;
    private Animation mAnimationBottomIn;
    private Animation mAnimationBottomOut;
    private Animation mSettingAnimationBottomOut;
    private boolean mIsFunctionAnimationEnd = true;

    private boolean mLightFollowSystem;

    private void hideStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        hideStatusBar();

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
        initAnimation();

        mBook = BookDataBase.getInstance(this).bookDao().getBook(id);
        if(mBook == null) {
            finish();
            return;
        }
        mBookReadView.setListener(this);
        mBookReadView.openBook(mBook);

        initState();
        updateScreenLight();
        initTextSize();
    }

    private void initState() {
        mEpisodeProgress.setMax(mBook.episode_count);
        mEpisodeProgress.setProgress(mBook.episode - 1);
        if(mBook.episode <= 1) {
            mPreviousView.setEnabled(false);
        } else {
            mPreviousView.setEnabled(true);
        }
        if(mBook.episode >= mBook.episode_count) {
            mNextView.setEnabled(false);
        } else {
            mNextView.setEnabled(true);
        }

        updateNightModeStats(SharePreferenceUtils.getIsNightMode(this));
    }

    public void updateScreenLight() {
        mLightFollowSystem = SharePreferenceUtils.getFollowSystemLight(ReadActivity.this);
        int light = SharePreferenceUtils.getScreenLight(ReadActivity.this);

        if(!mLightFollowSystem) {
            if(light == -1) {
                light = Utils.getSystemBrightness(ReadActivity.this);
                SharePreferenceUtils.saveScreenLight(ReadActivity.this, light);
            }
        }
        mSystemLightView.setSelected(mLightFollowSystem);
        mScreenLightView.setProgress(light);
    }

    private void initTextSize() {
        mTextSizeView.setText(String.valueOf(mBookReadView.getTextSizeType()));
    }

    private void updateNightModeStats(boolean isNight) {
        if(isNight) {
            mNightIconView.setImageResource(R.drawable.ic_day);
            mNightTitleView.setText("日间");
            mBookReadView.setBackgroundColor(R.color.black, R.color.white);
            mCurrentBgView.setImageResource(R.drawable.read_bg_black);
        } else {
            mNightIconView.setImageResource(R.drawable.ic_night);
            mNightTitleView.setText("夜间");
            int type = SharePreferenceUtils.getReadBackgroundColor(ReadActivity.this);
            mBookReadView.setBackgroundColor(ReadConfig.BG_COLORS[type], ReadConfig.TEXT_COLORS[type]);
            mCurrentBgView.setImageResource(ReadConfig.BG_COLOR_IMAGES[type]);
        }
        SharePreferenceUtils.saveIsNightMode(ReadActivity.this, isNight);
    }

    private void initView() {
        mBookReadView = findViewById(R.id.read_view);

        mTitleFunctionLayout = findViewById(R.id.title_function);
        mBackView = findViewById(R.id.function_back);
        mEpisodeTitleView = findViewById(R.id.episode_title);
        mBackView.setOnClickListener(mBackClickListener);

        mBottomFunctionLayout = findViewById(R.id.bottom_function);
        mNextView = findViewById(R.id.next_episode);
        mPreviousView = findViewById(R.id.previous_episode);
        mEpisodeProgress = findViewById(R.id.episode_progress);
        mCatalogBtn = findViewById(R.id.function_catalog);
        mSettingBtn = findViewById(R.id.function_setting);
        mNightBtn = findViewById(R.id.function_night);
        mNightIconView = findViewById(R.id.function_night_icon);
        mNightTitleView = findViewById(R.id.function_night_title);
        mNextView.setOnClickListener(mNextClickListener);
        mPreviousView.setOnClickListener(mPreviousClickListener);
        mCatalogBtn.setOnClickListener(mCatalogClickListener);
        mNightBtn.setOnClickListener(mNightClickListener);
        mSettingBtn.setOnClickListener(mSettingClickListener);
        mEpisodeProgress.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mSettingFunctionLayout = findViewById(R.id.setting_function);
        mScreenLightView = findViewById(R.id.screen_light);
        mSystemLightView = findViewById(R.id.light_system);
        mTextSizeView = findViewById(R.id.text_size);
        mCurrentBgView = findViewById(R.id.current_bg);
        for(int i = 0; i < TEXT_SIZE_CHANGE_IDS.length; i++) {
            TextView view = findViewById(TEXT_SIZE_CHANGE_IDS[i]);
            view.setTag(i);
            view.setOnClickListener(mChangeTextSizeClickListener);
        }
        for(int i = 0; i < BG_ICON_IDS.length; i++) {
            ImageView view = findViewById(BG_ICON_IDS[i]);
            view.setTag(i);
            view.setOnClickListener(mBgClickListener);
        }
        mScreenLightView.setOnSeekBarChangeListener(mLightSeekBarChangeListener);
        mSystemLightView.setOnClickListener(mLightFollowSystemListener);
    }

    private static final int[] BG_ICON_IDS = new int[]{
            R.id.read_bg_white,
            R.id.read_bg_blue,
            R.id.read_bg_brown,
            R.id.read_bg_gray,
            R.id.read_bg_green
    };

    private static final int[] TEXT_SIZE_CHANGE_IDS = new int[]{
            R.id.text_size_add,
            R.id.text_size_reduce
    };

    private void initAnimation() {
        mAnimationTopIn = AnimationUtils.loadAnimation(this, R.anim.top_in);
        mAnimationTopOut = AnimationUtils.loadAnimation(this, R.anim.top_out);
        mAnimationBottomIn = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        mAnimationBottomOut = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        mSettingAnimationBottomOut = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        mAnimationTopIn.setAnimationListener(mFunctionInAnimationListener);
        mAnimationBottomIn.setAnimationListener(mFunctionInAnimationListener);
        mAnimationTopOut.setAnimationListener(mFunctionOutAnimationListener);
        mAnimationBottomOut.setAnimationListener(mFunctionOutAnimationListener);
    }

    private boolean checkAnimation() {
        return mAnimationTopIn != null && mAnimationTopOut != null
                && mAnimationBottomIn != null && mAnimationBottomOut != null;
    }

    private SeekBar.OnSeekBarChangeListener mLightSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int light;
            if(mLightFollowSystem) {
                light = -1;
                mLightFollowSystem = false;
            } else {
                if(progress <= 0) {
                    light = 1;
                } else if(progress > 255) {
                    light = 255;
                } else {
                    light = progress;
                }
                SharePreferenceUtils.setFollowSystemLight(ReadActivity.this, false);
                SharePreferenceUtils.saveScreenLight(ReadActivity.this, light);
                mSystemLightView.setSelected(false);
            }
            Utils.changeAppBrightness(ReadActivity.this, light);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(progress >= 0 && progress < mBook.episode_count) {
                mBookReadView.setEpisode(progress + 1);
                updateEpisodeTitle();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private View.OnClickListener mLightFollowSystemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean selected = mSystemLightView.isSelected();
            if(selected) {
                SharePreferenceUtils.setFollowSystemLight(ReadActivity.this, false);
            } else {
                SharePreferenceUtils.setFollowSystemLight(ReadActivity.this, true);
            }
            updateScreenLight();
        }
    };

    private View.OnClickListener mBgClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int type = (int)v.getTag();
            SharePreferenceUtils.saveReadBackgroundColor(ReadActivity.this, type);
            updateNightModeStats(false);
        }
    };

    private View.OnClickListener mChangeTextSizeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == TEXT_SIZE_CHANGE_IDS[0]) {
                mBookReadView.addTextSize();
            } else {
                mBookReadView.reduceTextSize();
            }
            mTextSizeView.setText(String.valueOf(mBookReadView.getTextSizeType()));
        }
    };

    private View.OnClickListener mBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener mNextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEpisodeProgress.setProgress(mEpisodeProgress.getProgress() + 1);
        }
    };

    private View.OnClickListener mPreviousClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEpisodeProgress.setProgress(mEpisodeProgress.getProgress() - 1);
        }
    };

    private View.OnClickListener mCatalogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveHistory();
            hideFunction();
            Intent intent = new Intent(ReadActivity.this, CatalogActivity.class);
            intent.putExtra("id", mBook.id);
            startActivityForResult(intent, REQUEST_CODE_CATALOG);
        }
    };

    private View.OnClickListener mNightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            updateNightModeStats(!SharePreferenceUtils.getIsNightMode(ReadActivity.this));
        }
    };

    private View.OnClickListener mSettingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showFunctionSetting();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        saveHistory();
    }

    private void saveHistory() {
        if(mBook == null) {
            mBook = mBookReadView.getBook();
        }
        mBook.timestamp = System.currentTimeMillis();
        BookDataBase.getInstance(this).bookDao().update(mBook);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BoradcastAction.BOOK_RECORD_CHANGED));
    }

    private void showFunction() {
        showStatusBar();
        mTitleFunctionLayout.setVisibility(View.VISIBLE);
        mBottomFunctionLayout.setVisibility(View.VISIBLE);
        mTitleFunctionLayout.startAnimation(mAnimationTopIn);
        mBottomFunctionLayout.startAnimation(mAnimationBottomIn);
    }

    private void showFunctionSetting() {
        mIsFunctionAnimationEnd = false;
        mSettingFunctionLayout.setVisibility(View.VISIBLE);
        mSettingFunctionLayout.startAnimation(mAnimationBottomIn);
    }

    private void hideFunction() {
        hideStatusBar();
        mTitleFunctionLayout.startAnimation(mAnimationTopOut);
        mBottomFunctionLayout.startAnimation(mAnimationBottomOut);
        if(mSettingFunctionLayout.getVisibility() == View.VISIBLE) {
            mSettingFunctionLayout.startAnimation(mSettingAnimationBottomOut);
        }
    }

    private Animation.AnimationListener mFunctionOutAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mTitleFunctionLayout.setVisibility(View.GONE);
            mBottomFunctionLayout.setVisibility(View.GONE);
            mSettingFunctionLayout.setVisibility(View.GONE);
            mIsFunctionAnimationEnd = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mFunctionInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mIsFunctionAnimationEnd = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    public void onShowMenu() {
        if(mIsFunctionAnimationEnd) {
            mIsFunctionAnimationEnd = false;
            if(!checkAnimation()) {
                initAnimation();
            }
            if(mTitleFunctionLayout.getVisibility() == View.VISIBLE) {
                hideFunction();
            } else {
                updateEpisodeTitle();
                showFunction();
            }
        }
    }

    @Override
    public boolean menuOnShow() {
        return mTitleFunctionLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onGetCurrent(int episode, boolean success) {
    }

    @Override
    public void onGetNext(int episode, boolean success) {
    }

    @Override
    public void onGetPrevious(int episode, boolean success) {
    }

    private void updateEpisodeTitle() {
        mEpisodeTitleView.setText(mBookReadView.getCurrentEpisodeTitle());
    }

    private static final int REQUEST_CODE_CATALOG = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CATALOG) {
            if(resultCode == RESULT_OK) {
                int episode = data.getIntExtra("episode", 0);
                if(episode > 0) {
                    mEpisodeProgress.setProgress(episode - 1);
                }
            }
        }
    }
}
