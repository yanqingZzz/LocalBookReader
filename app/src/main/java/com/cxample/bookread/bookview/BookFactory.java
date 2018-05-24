package com.cxample.bookread.bookview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.text.TextPaint;
import android.util.Log;

import com.cxample.bookread.R;
import com.cxample.bookread.activity.OnReadViewListener;
import com.cxample.bookread.constant.ReadConfig;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.Episode;
import com.cxample.bookread.db.EpisodeDataBase;
import com.cxample.bookread.utils.FileUtils;
import com.cxample.bookread.utils.SharePreferenceUtils;

import java.util.ArrayList;

/**
 * Created by yanqing on 2018/5/18.
 */

public class BookFactory {
    public enum TYPE {CURRENT, NEXT, PREVIOUS}

    private Context mContext;

    //绘制小说内容Paint
    private Paint mTextPaint;
    //绘制小说标题Paint
    private Paint mTitlePaint;
    //绘制背景Paint
    private Paint mBgPaint;
    //显示view 的高宽
    private int mReadViewWidth;
    private int mReadViewHeight;
    //标题的高度
    private int mTitleHeight;
    //内容字体的大小
    private int mTextSizeIndex;
    private int mTextSize;
    //标题字体的大小
    private int mTitleSize;
    //内容的字体颜色
    private int mTextColor;
    //标题的字体颜色
    private int mTitleColor;
    //行间距
    private int mLineSpace;
    //一页的显示内容的最大行数
    private int mPageLineCount;
    //背景颜色
    private int mBgColor;

    private int mTitlePadding;
    private int mContentPadding;

    //小说的信息
    private Book mBook;
    private Episode mEpisode;
    private Episode mNextEpisode;
    private Episode mPreviousEpisode;

    private ArrayList<String> mPageLines;    //当前页显示的内容
    private ArrayList<String> mNextPageLines;    //下一页的内容
    private ArrayList<String> mPreviousPageLines;    //上一页的内容

    private ArrayList<String> mContentList;    //当前分集的内容
    private ArrayList<String> mNextContentList;    //下一分集的内容
    private ArrayList<String> mPreviousContentList;    //上一分集的内容

    //回调接口
    private OnReadViewListener mReadViewListener;

    public BookFactory(Context context, int width, int height) {
        mContext = context;
        init(width, height);
    }

    //初始化默认的配置
    public void init(int width, int height) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint = new Paint();
        mReadViewWidth = width;
        mReadViewHeight = height;
        mTitleSize = (int)mContext.getResources().getDimension(R.dimen.book_read_view_default_title_size);
        mTextSizeIndex = SharePreferenceUtils.getTextSize(mContext);
        mTextSize = (int)mContext.getResources().getDimension(ReadConfig.TEXT_SIZE[mTextSizeIndex][ReadConfig.TEXT_SIZE_COLUM]);
        mTitlePadding = (int)mContext.getResources().getDimension(R.dimen.book_read_view_default_title_padding);
        mContentPadding = (int)mContext.getResources().getDimension(R.dimen.book_read_view_default_content_padding);
        mLineSpace = (int)mContext.getResources().getDimension(R.dimen.book_read_view_default_line_space);
        mTextColor = Color.BLACK;
        mTitleColor = Color.GRAY;
        mBgColor = Color.WHITE;
        updateConfig();
    }

    //配置信息有改动时，重新计算相关参数
    private void updateConfig() {
        mTitleHeight = mTextSize + mTitlePadding * 2;
        int bottomFunctionHeight = mTextSize + mTitlePadding * 2;
        mPageLineCount = (mReadViewHeight + mLineSpace - mTitleHeight - bottomFunctionHeight) / (mLineSpace + mTextSize);
        mTextPaint.setTextSize(mTextSize);
        mTitlePaint.setTextSize(mTitleSize);
        mTextPaint.setColor(mTextColor);
        mTitlePaint.setColor(mTitleColor);
        mBgPaint.setColor(mBgColor);
        if(mBook != null) {
            openBook(mBook);
        }
    }


    public boolean addTextSize() {
        if(mTextSizeIndex < ReadConfig.TEXT_SIZE.length - 1) {
            mTextSizeIndex++;
            mTextSize = (int)mContext.getResources().getDimension(ReadConfig.TEXT_SIZE[mTextSizeIndex][ReadConfig.TEXT_SIZE_COLUM]);
            updateConfig();
            SharePreferenceUtils.saveTextSize(mContext, mTextSizeIndex);
            return true;
        }
        return false;
    }

    public boolean reduceTextSize() {
        if(mTextSizeIndex > 0) {
            mTextSizeIndex--;
            mTextSize = (int)mContext.getResources().getDimension(ReadConfig.TEXT_SIZE[mTextSizeIndex][ReadConfig.TEXT_SIZE_COLUM]);
            updateConfig();
            SharePreferenceUtils.saveTextSize(mContext, mTextSizeIndex);
            return true;
        } else {
            return false;
        }
    }

    public int getTextSizeType() {
        return ReadConfig.TEXT_SIZE[mTextSizeIndex][ReadConfig.TEXT_SIZE_TYPE_COLUM];
    }

    public void setLineSpace(int lineSpace) {
        mLineSpace = lineSpace;
        updateConfig();
    }

    public void setBackgroundColor(@ColorInt int bgColor, @ColorInt int textColor) {
        mBgColor = bgColor;
        mTextColor = textColor;
        updateConfig();
    }

    public void setReadViewListener(OnReadViewListener readViewListener) {
        mReadViewListener = readViewListener;
    }

    public Book getBook() {
        return mBook;
    }

    public String getCurrentEpisodeTitle() {
        if(mEpisode != null) return mEpisode.title;
        return "";
    }

    //打开书籍
    public void openBook(Book book) {
        if(mContext == null || book == null) {
            Log.e("BookFactory", "openBook: no init or book is null");
            return;
        }
        mBook = book;
        //初始化当前章节的内容
        getCurrentEpisode(mBook.episode);
        //初始化三页的内容
        mPageLines = getCurrentPageLine(mBook.page);
        mNextPageLines = getNextPageLines(mBook.page + 1);
        mPreviousPageLines = getPreviousPageLines(mBook.page - 1);
    }

    public void setEpisode(int episode) {
        if(mBook == null) return;
        if(episode < 1) {
            episode = 1;
        } else if(episode > mBook.episode_count) {
            episode = mBook.episode_count;
        }
        mBook.episode = episode;
        mBook.page = 0;
        clearCache();
        openBook(mBook);
    }

    private void clearCache() {
        mContentList = null;
        mNextContentList = null;
        mPreviousContentList = null;
        mPageLines = null;
        mNextPageLines = null;
        mPreviousPageLines = null;
    }

    //绘制当前页面的内容
    public void drawBitmap(Canvas canvas) {
        drawBitmap(canvas, TYPE.CURRENT);
    }

    //绘制指定页面的内容，TYPE（CURRENT, NEXT, PREVIOUS）
    public void drawBitmap(Canvas canvas, TYPE type) {
        ArrayList<String> pageLines = null;
        int page = 0;
        switch(type) {
            case CURRENT: {
                pageLines = mPageLines;
                page = mBook.page + 1;
                break;
            }
            case NEXT: {
                pageLines = mNextPageLines;
                page = mBook.page + 2;
                break;
            }
            case PREVIOUS: {
                pageLines = mPreviousPageLines;
                page = mBook.page;
                break;
            }
        }
        if(pageLines == null || pageLines.size() == 0) return;

        //绘制背景
        canvas.drawRect(0, 0, mReadViewWidth, mReadViewHeight, mBgPaint);

        //计算当前集有多少页
        int count = (int)Math.ceil(mContentList.size() / (float)mPageLineCount);
        //绘制阅读进度（已读页/总共页）
        if(mContentList != null && mContentList.size() > 0) {
            String progress;
            if(page > count) {
                int realCount = (int)Math.ceil(mNextContentList.size() / (float)mPageLineCount);
                progress = "1/" + realCount;
            } else if(page < 0) {
                int realCount = (int)Math.ceil(mPreviousPageLines.size() / (float)mPageLineCount);
                progress = realCount + "/" + realCount;
            } else {
                progress = page + "/" + count;
            }

            int startX = (mReadViewWidth - getStrWidth(progress, mTitleSize)) / 2;
            int startY = mReadViewHeight - mTitlePadding;
            canvas.drawText(progress, startX, startY, mTitlePaint);
        }

        //绘制分集标题
        Episode episode;
        if(page < 0) {
            episode = mPreviousEpisode;
        } else if(page > count) {
            episode = mNextEpisode;
        } else {
            episode = mEpisode;
        }
        if(episode != null) {
            canvas.drawText(episode.title, mTitlePadding, mTextSize, mTitlePaint);
        }

        //绘制具体内容
        int height = mTextSize + mTitleHeight;
        for(String line : pageLines) {
            canvas.drawText(line, mContentPadding, height, mTextPaint);
            height = height + mTextSize + mLineSpace;
        }
    }

    //获取一个字符串的宽度
    private int getStrWidth(String str, int textSize) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        return (int)paint.measureText(str);
    }

    //判断是否有下一页
    public boolean hasNextPage() {
        return mNextPageLines != null && mNextPageLines.size() > 0;
    }

    //判断是否有上一页
    public boolean hasPreviousPage() {
        return mPreviousPageLines != null && mPreviousPageLines.size() > 0;
    }

    //跳转到下一页
    public void setNext() {
        mPreviousPageLines = mPageLines;
        mPageLines = getCurrentPageLine(++mBook.page);
        mNextPageLines = getNextPageLines(mBook.page + 1);
    }

    //跳转到上一页
    public void setPrevious() {
        mNextPageLines = mPageLines;
        mPageLines = getCurrentPageLine(--mBook.page);
        mPreviousPageLines = getPreviousPageLines(mBook.page - 1);
    }

    //获取当前分集的内容
    private void getCurrentEpisode(int index) {
        mContentList = new ArrayList<>();
        mEpisode = EpisodeDataBase.getInstance(mContext).episodeDao().getEpisodeByIndex(mBook.id, index);

        //获取到内容，并将内容分割成行
        ArrayList<String> contents = FileUtils.getEpisodeContent(mContext, mBook, index);
        if(contents != null && contents.size() > 0) {
            for(String content : contents) {
//            content = content.replaceAll("\r", " ").replaceAll("\n", " "); // 段落中的换行符去掉，绘制的时候再换行
                while(content.length() > 0) {
                    int size = mTextPaint.breakText(content, true, mReadViewWidth - mContentPadding * 2, null);
                    mContentList.add(content.substring(0, size));
                    content = content.substring(size);
                }
            }
        }
        if(mReadViewListener != null) {
            if(mContentList != null && mContentList.size() > 0) {
                mReadViewListener.onGetCurrent(index, true);
            } else {
                mReadViewListener.onGetCurrent(index, false);
            }
        }
    }

    //获取当前下一集的内容
    private void getNextEpisode(int index) {
        mNextContentList = new ArrayList<>();
        if(index >= mBook.episode_count) return;
        mNextEpisode = EpisodeDataBase.getInstance(mContext).episodeDao().getEpisodeByIndex(mBook.id, index);

        //获取到内容，并将内容分割成行
        ArrayList<String> contents = FileUtils.getEpisodeContent(mContext, mBook, index);
        if(contents != null && contents.size() > 0) {
            for(String content : contents) {
                while(content.length() > 0) {
                    int size = mTextPaint.breakText(content, true, mReadViewWidth - mContentPadding * 2, null);
                    mNextContentList.add(content.substring(0, size));
                    content = content.substring(size);
                }
            }
        }

        if(mReadViewListener != null) {
            if(mNextContentList != null && mNextContentList.size() > 0) {
                mReadViewListener.onGetNext(index, true);
            } else {
                mReadViewListener.onGetNext(index, false);
            }
        }
    }

    //获取当前上一集的内容
    private void getPreviousEpisode(int index) {
        mPreviousContentList = new ArrayList<>();
        if(index < 0) return;
        mPreviousEpisode = EpisodeDataBase.getInstance(mContext).episodeDao().getEpisodeByIndex(mBook.id, index);

        //获取到内容，并将内容分割成行
        ArrayList<String> contents = FileUtils.getEpisodeContent(mContext, mBook, index);
        if(contents != null && contents.size() > 0) {
            for(String content : contents) {
                while(content.length() > 0) {
                    int size = mTextPaint.breakText(content, true, mReadViewWidth - mContentPadding * 2, null);
                    mPreviousContentList.add(content.substring(0, size));
                    content = content.substring(size);
                }
            }
        }

        if(mReadViewListener != null) {
            if(mPreviousContentList != null && mPreviousContentList.size() > 0) {
                mReadViewListener.onGetPrevious(index, true);
            } else {
                mReadViewListener.onGetPrevious(index, false);
            }
        }
    }

    //获取当前页的内容
    private ArrayList<String> getCurrentPageLine(int page) {
        if(mContentList == null || mContentList.size() == 0) return null;

        ArrayList<String> lines = new ArrayList<>();
        int count = (int)Math.ceil(mContentList.size() / (float)mPageLineCount);
        if(page >= count) {
            //如果page超过了最大数，需要跳转到下一集
            mPreviousContentList = mContentList;
            mContentList = mNextContentList;
            mNextContentList = null;

            mBook.episode++;
            mBook.page = 0;
            mEpisode = mNextEpisode;
            page = mBook.page;

            lines = mNextPageLines;
            mNextPageLines = null;
        } else if(page < 0) {
            //如果page小于0，需要跳转到上一集
            mNextContentList = mContentList;
            mContentList = mPreviousContentList;
            mPreviousContentList = null;

            mBook.episode--;
            mBook.page = (int)Math.ceil(mContentList.size() / (float)mPageLineCount) - 1;
            mEpisode = mPreviousEpisode;
            page = mBook.page;

            lines = mPreviousPageLines;
            mPreviousPageLines = null;
        }

        if(lines != null && lines.size() > 0) {
            return lines;
        }

        //如果lines为空，则尝试重新获取
        if(mContentList != null && mContentList.size() > 0) {
            int index = page * mPageLineCount;
            int maxIndex = index + mPageLineCount;
            if(index < mContentList.size()) {
                for(; index < mContentList.size() && index < maxIndex; index++) {
                    lines.add(mContentList.get(index));
                }
                return lines;
            }
        }
        return null;
    }

    //获取下一页的内容
    private ArrayList<String> getNextPageLines(int page) {
        ArrayList<String> lines = new ArrayList<>();
        int count = (int)Math.ceil(mContentList.size() / (float)mPageLineCount);
        if(page < count) {
            //如果page在当前分集的范围内，就在当前分集的内容中取
            int index = page * mPageLineCount;
            int maxIndex = index + mPageLineCount;
            for(; index < mContentList.size() && index < maxIndex; index++) {
                lines.add(mContentList.get(index));
            }
        } else {
            //如果page超出了当前分集，则到下一集中获取
            //如果下一级的内容为空，尝试去获取下一级的内容
            if(mNextContentList == null || mNextContentList.size() == 0) {
                getNextEpisode(mBook.episode + 1);
            }
            if(mNextContentList != null && mNextContentList.size() > 0) {
                //获取到下一集内容
                int maxIndex = mPageLineCount;
                for(int index = 0; index < mNextContentList.size() && index < maxIndex; index++) {
                    lines.add(mNextContentList.get(index));
                }
            } else {
                //获取下一级内容失败
            }
        }
        return lines;
    }

    //获取上一页的内容
    private ArrayList<String> getPreviousPageLines(int page) {
        ArrayList<String> lines = new ArrayList<>();
        if(page >= 0) {
            int index = page * mPageLineCount;
            int maxIndex = index + mPageLineCount;
            for(; index < mContentList.size() && index < maxIndex; index++) {
                lines.add(mContentList.get(index));
            }
        } else {
            if(mPreviousContentList == null || mPreviousContentList.size() == 0) {
                getPreviousEpisode(mBook.episode - 1);
            }
            if(mPreviousContentList != null && mPreviousContentList.size() > 0) {
                page = (int)Math.ceil(mPreviousContentList.size() / (float)mPageLineCount) - 1;
                int index = page * mPageLineCount;
                int maxIndex = index + mPageLineCount;
                for(; index < mPreviousContentList.size() && index < maxIndex; index++) {
                    lines.add(mPreviousContentList.get(index));
                }
            } else {
                //获取下一级内容失败
            }
        }
        return lines;
    }
}
