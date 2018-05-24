package com.cxample.bookread.bookview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.cxample.bookread.R;
import com.cxample.bookread.activity.OnReadViewListener;
import com.cxample.bookread.db.Book;

/**
 * Created by yanqing on 2018/5/18.
 */

public abstract class BaseReadView extends View {
    private static final int SCROLL_DURATION = 400;

    //当前、上一页、下一页的bitmap和canvas
    private Bitmap mCurrentBitmap;
    private Bitmap mNextBitmap;
    private Bitmap mPreviousBitmap;
    private Canvas mCurrentCanvas;
    private Canvas mNextCanvas;
    private Canvas mPreviousCanvas;

    private BookFactory mBookFactory;
    private OnReadViewListener mListener;

    //是否需要重新绘制内容，做切页动画是不需要重复的绘制内容
    private boolean mNeedUpdateContent = true;
    //滚动动画是否结束
    private boolean mScrollFinish = true;

    protected int mViewWidth;
    protected int mViewHeight;
    protected int mShadowWidth;

    protected Scroller mScroller;

    public BaseReadView(Context context) {
        super(context);
        init(context);
    }

    public BaseReadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        setBackgroundColor(Color.WHITE);
        setClickable(true);
        WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        mViewWidth = metrics.widthPixels;
        mViewHeight = metrics.heightPixels;

        mShadowWidth = (int)context.getResources().getDimension(R.dimen.book_read_normal_view_shadow_width);

        mBookFactory = new BookFactory(context, mViewWidth, mViewHeight);
        mScroller = new Scroller(context, new LinearInterpolator());
    }

    //打开书籍
    public void openBook(Book book) {
        mBookFactory.openBook(book);
        invalidate();
    }

    public void setEpisode(int episode) {
        mBookFactory.setEpisode(episode);
        setContentNeedUpdate();
        invalidate();
    }

    public void setBackgroundColor(int bgColor, int textColor) {
        mBookFactory.setBackgroundColor(getResources().getColor(bgColor), getResources().getColor(textColor));
        setContentNeedUpdate();
        invalidate();
    }

    public void addTextSize() {
        if(mBookFactory.addTextSize()){
            setContentNeedUpdate();
            invalidate();
        }
    }

    public void reduceTextSize() {
        if(mBookFactory.reduceTextSize()){
            setContentNeedUpdate();
            invalidate();
        }
    }

    public int getTextSizeType() {
        return mBookFactory.getTextSizeType();
    }

    public Book getBook() {
        return mBookFactory.getBook();
    }

    public String getCurrentEpisodeTitle() {
        return mBookFactory.getCurrentEpisodeTitle();
    }

    public void setListener(OnReadViewListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent();
        drawBitmap(canvas);
        drawShadow(canvas);
    }

    //绘制内容
    private void drawContent() {
        if(mNeedUpdateContent) {
            drawNextContent();
            drawCurrentContent();
            drawPreviousContent();
            mNeedUpdateContent = false;
        }
    }

    //将bitmap绘制到canvas
    private void drawBitmap(Canvas canvas) {
        drawNextBitmap(canvas, mNextBitmap);
        drawCurrentBitmap(canvas, mCurrentBitmap);
        drawPreviousBitmap(canvas, mPreviousBitmap);
    }

    //绘制当前页的内容
    private void drawCurrentContent() {
        if(mCurrentBitmap == null) {
            mCurrentBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        }
        if(mCurrentCanvas == null) {
            mCurrentCanvas = new Canvas(mCurrentBitmap);
        } else {
            mCurrentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        mBookFactory.drawBitmap(mCurrentCanvas);
    }

    //绘制下一页的内容
    private void drawNextContent() {
        if(mNextBitmap == null) {
            mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        }
        if(mNextCanvas == null) {
            mNextCanvas = new Canvas(mNextBitmap);
        } else {
            mNextCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        mBookFactory.drawBitmap(mNextCanvas, BookFactory.TYPE.NEXT);
    }

    //绘制上一页的内容
    private void drawPreviousContent() {
        if(mPreviousBitmap == null) {
            mPreviousBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        }
        if(mPreviousCanvas == null) {
            mPreviousCanvas = new Canvas(mPreviousBitmap);
        } else {
            mPreviousCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        mBookFactory.drawBitmap(mPreviousCanvas, BookFactory.TYPE.PREVIOUS);
    }

    //设置需要更新内容
    protected void setContentNeedUpdate() {
        mNeedUpdateContent = true;
    }

    protected boolean hasNextPage() {
        return mBookFactory.hasNextPage();
    }

    protected boolean hasPreviousPage() {
        return mBookFactory.hasPreviousPage();
    }

    protected void setNext() {
        mBookFactory.setNext();
        setContentNeedUpdate();
    }

    protected void setPrevious() {
        mBookFactory.setPrevious();
        setContentNeedUpdate();
    }

    //开始翻页的滚动动画
    protected void startScroll(int startX, int dx) {
        mScroller.startScroll(startX, 0, dx, 0, SCROLL_DURATION);
        mScrollFinish = false;
        invalidate();
    }

    protected void showMenu() {
        if(mListener != null) {
            mListener.onShowMenu();
        }
    }

    protected boolean menuOnShow() {
        return mListener != null && mListener.menuOnShow();
    }

    //是否是跳转下一页，true为下一页，false为跳转上一页
    private boolean isNext = true;

    protected boolean getIsNext() {
        return isNext;
    }

    protected void setIsNext(boolean isNext) {
        this.isNext = isNext;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(!mScrollFinish) {
            if(mScroller.isFinished()) {
                mScrollFinish = true;
                scrollFinished(isNext);
            } else if(mScroller.computeScrollOffset()) {
                scroll(mScroller.getCurrX(), isNext);
            }
        }
    }


    protected enum LOCATION {LEFT, CENTER, RIGHT}

    //check点击的位置
    protected LOCATION checkLocation(int x) {
        if(x < mViewWidth / 3) {
            return LOCATION.LEFT;
        } else if(x > mViewWidth / 3 * 2) {
            return LOCATION.RIGHT;
        } else {
            return LOCATION.CENTER;
        }
    }

    protected void scrollFinished(boolean isNext) {}

    protected void scroll(int currentX, boolean isNext) {}

    protected boolean isScrollFinish() {
        return mScrollFinish;
    }

    //抽象方法，由子类实现
    protected abstract void drawCurrentBitmap(Canvas canvas, Bitmap bitmap);

    protected abstract void drawNextBitmap(Canvas canvas, Bitmap bitmap);

    protected abstract void drawPreviousBitmap(Canvas canvas, Bitmap bitmap);

    protected abstract void drawShadow(Canvas canvas);

}
