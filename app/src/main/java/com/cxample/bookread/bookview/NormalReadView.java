package com.cxample.bookread.bookview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created by yanqing on 2018/5/21.
 */

public class NormalReadView extends BaseReadView {
    private int mCurBitmapLeft;
    private int mPreBitmapLeft;

    private GradientDrawable mShadowDrawable;

    private boolean mNeedShowShadow = false;

    public NormalReadView(Context context) {
        super(context);
    }

    public NormalReadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCurBitmapLeft = 0;
        mPreBitmapLeft = -mViewWidth;
    }

    @Override
    protected void drawCurrentBitmap(Canvas canvas, Bitmap bitmap) {
        if(bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, mCurBitmapLeft, 0, null);
        }
    }

    @Override
    protected void drawNextBitmap(Canvas canvas, Bitmap bitmap) {
        if(bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    @Override
    protected void drawPreviousBitmap(Canvas canvas, Bitmap bitmap) {
        if(bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, mPreBitmapLeft, 0, null);
        }
    }

    @Override
    protected void drawShadow(Canvas canvas) {
        if(mNeedShowShadow) {
            initShadow();
            int left, right;
            if(getIsNext()) {
                left = mCurBitmapLeft + mViewWidth - mShadowWidth;
                right = mCurBitmapLeft + mViewWidth;
            } else {
                left = mPreBitmapLeft + mViewWidth - mShadowWidth;
                right = mPreBitmapLeft + mViewWidth;
            }
            mShadowDrawable.setBounds(left, 0, right, mViewHeight);
            mShadowDrawable.draw(canvas);
        }
    }

    private void initShadow() {
        if(mShadowDrawable == null) {
            int[] color = {0x333333, 0xb0333333};
            mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
            mShadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        }
    }

    @Override
    protected void scroll(int currentX, boolean isNext) {
        super.scroll(currentX, isNext);
        mNeedShowShadow = true;
        if(isNext) {
            mCurBitmapLeft = currentX;
        } else {
            mPreBitmapLeft = currentX;
        }
        invalidate();
    }

    @Override
    protected void scrollFinished(boolean isNext) {
        super.scrollFinished(isNext);
        mNeedShowShadow = false;
        if(isChangePage) {
            if(isNext) {
                mCurBitmapLeft = 0;
                setNext();
            } else {
                mPreBitmapLeft = -mViewWidth;
                setPrevious();
            }

        }
        invalidate();
    }


    private enum SLIDING_DIRECTION {LEFT, RIGHT, INVALID}

    private int mTouchX;
    private boolean isChangePage = false;
    private SLIDING_DIRECTION mSlidingDirection = SLIDING_DIRECTION.INVALID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchX = x;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //现在没有在翻页
                if(isScrollFinish() && !menuOnShow()) {
                    int distance = x - mTouchX;
                    if(mSlidingDirection != SLIDING_DIRECTION.INVALID || (Math.abs(distance) > 50)) {
                        //第一次判断到MOVE动作，设置跳转方向（上一页，下一页）
                        if(mSlidingDirection == SLIDING_DIRECTION.INVALID) {
                            setIsNext(distance < 0);
                        }
                        //记录本次滑动的方向
                        if(distance < -50) {
                            mSlidingDirection = SLIDING_DIRECTION.LEFT;
                        } else if(distance > 50) {
                            mSlidingDirection = SLIDING_DIRECTION.RIGHT;
                        }

                        mTouchX = x;

                        if(getIsNext() && hasNextPage()) { //如果是下一页并且有下一页，就对当前页做跟随手指滑动的动画
                            mCurBitmapLeft += distance;

                            if(mCurBitmapLeft > 0) {
                                mCurBitmapLeft = 0;
                            } else if(mCurBitmapLeft < -mViewWidth) {
                                mCurBitmapLeft = -mViewWidth;
                            }
                            mNeedShowShadow = true;
                            invalidate();
                        } else if(hasPreviousPage()) { //如果是上一页并且有上一页，就对上一页做跟随手指滑动的动画
                            mPreBitmapLeft += distance;
                            if(mPreBitmapLeft > 0) {
                                mPreBitmapLeft = 0;
                            } else if(mPreBitmapLeft < -mViewWidth) {
                                mPreBitmapLeft = -mViewWidth;
                            }
                            mNeedShowShadow = true;
                            invalidate();
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if(isScrollFinish()) {
                    if(menuOnShow()) {
                        showMenu();
                    } else {
                        //判断是点击还是滑动
                        if(mSlidingDirection != SLIDING_DIRECTION.INVALID) { //滑动
                            //判断是上一页还是下一页
                            if(getIsNext()) { // 下一页
                                if(mSlidingDirection == SLIDING_DIRECTION.LEFT) { //向左翻页
                                    //判断为跳转到下一页，将剩余的部分滑动出屏幕
                                    isChangePage = true;
                                    if(hasNextPage()) {
                                        startScroll(mCurBitmapLeft, -(mViewWidth - mCurBitmapLeft));
                                    } else {
                                        Toast.makeText(getContext(), "没有下一页", Toast.LENGTH_SHORT).show();
                                    }
                                } else if(mSlidingDirection == SLIDING_DIRECTION.RIGHT) {
                                    //判断为放弃跳转下一页，将当前页还原
                                    isChangePage = false;
                                    startScroll(mCurBitmapLeft, -mCurBitmapLeft);
                                }
                            } else {
                                //翻页到上一页，逻辑与下一页相同
                                if(mSlidingDirection == SLIDING_DIRECTION.RIGHT) {
                                    isChangePage = true;
                                    if(hasPreviousPage()) {
                                        startScroll(mPreBitmapLeft, -mPreBitmapLeft);
                                    } else {
                                        Toast.makeText(getContext(), "没有上一页", Toast.LENGTH_SHORT).show();
                                    }
                                } else if(mSlidingDirection == SLIDING_DIRECTION.LEFT) {
                                    isChangePage = false;
                                    startScroll(mPreBitmapLeft, mPreBitmapLeft);
                                }
                            }
                            //将滑动的方向重置
                            mSlidingDirection = SLIDING_DIRECTION.INVALID;
                        } else {
                            //点击翻页
                            switch(checkLocation(x)) {
                                case RIGHT: {
                                    if(hasNextPage()) {
                                        isChangePage = true;
                                        setIsNext(true);
                                        startScroll(mCurBitmapLeft, -(mViewWidth - mCurBitmapLeft));
                                    } else {
                                        Toast.makeText(getContext(), "没有下一页", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                                case LEFT: {
                                    if(hasPreviousPage()) {
                                        isChangePage = true;
                                        setIsNext(false);
                                        startScroll(mPreBitmapLeft, -mPreBitmapLeft);
                                    } else {
                                        Toast.makeText(getContext(), "没有上一页", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                                case CENTER: {
                                    showMenu();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

}
