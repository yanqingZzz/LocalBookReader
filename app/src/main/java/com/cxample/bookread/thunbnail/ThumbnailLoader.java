package com.cxample.bookread.thunbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yanqing on 2018/4/12.
 */

public class ThumbnailLoader {
    private static final int MESSAGE_LOAD_START = 0;
    private static final int MESSAGE_LOAD_FINISH = 1;

    private static final int mThumbnailWidth = 96;
    private static final int mThumbnailHeight = 96;
    private static final int THREAD_POOL_NUM = 5;

    private static ExecutorService sExecutorService;
    private static LoadThumbnailHandler sThumbnailHandler;
    private static ConcurrentHashMap<View, LoadThumbnailTask> sTaskConcurrentHashMap;

    static {
//        sExecutorService = Executors.newCachedThreadPool();
        sExecutorService = Executors.newFixedThreadPool(THREAD_POOL_NUM);
        sThumbnailHandler = new LoadThumbnailHandler();
        sTaskConcurrentHashMap = new ConcurrentHashMap<>();
    }


    public static void loadImageThumbnail(String path, ImageView view, int defaultImageResId) {
        loadThumbnail(path, view, defaultImageResId, LoadThumbnailTask.THUMBNAIL_TYPE_IMAGE);
    }

    public static void LoadVideoThumbnail(String path, ImageView view, int defaultImageResId) {
        loadThumbnail(path, view, defaultImageResId, LoadThumbnailTask.THUMBNAIL_TYPE_VIDEO);
    }

    private static void loadThumbnail(String path, ImageView view, int defaultImageResId, int type) {
        removeTask(view);

        Bitmap cacheBitmap = ThumbnailCache.getBitmapCache(path);
        if(cacheBitmap != null) {
            showBitmapToView(view, cacheBitmap);
        } else {
            LoadThumbnailTask task = new LoadThumbnailTask(view, path, defaultImageResId);
            task.setSize(mThumbnailWidth, mThumbnailHeight);
            task.setType(type);
            addTask(task);
            sExecutorService.execute(task);
        }
    }

    private static void addTask(LoadThumbnailTask task) {
        if(task != null && task.mImageView != null) {
            sTaskConcurrentHashMap.put(task.mImageView, task);
        }
    }

    private static void removeTask(ImageView view) {
        LoadThumbnailTask task = sTaskConcurrentHashMap.get(view);
        if(task != null) {
            task.cancel();
            sTaskConcurrentHashMap.remove(view);
        }
    }

    private static class LoadThumbnailTask implements Runnable {
        private static int THUMBNAIL_TYPE_IMAGE = 0;
        private static int THUMBNAIL_TYPE_VIDEO = 1;

        private ImageView mImageView;
        private String mPath;
        private int mDefaultImageResId;
        private int mWidth;
        private int mHeight;

        private Bitmap mBitmap;
        private int mType = THUMBNAIL_TYPE_IMAGE;
        private boolean mIsCancel = false;

        private LoadThumbnailTask(ImageView imageView, String path, int defaultImageResId) {
            mImageView = imageView;
            mPath = path;
            mDefaultImageResId = defaultImageResId;
            showDefaultImage();
        }

        private LoadThumbnailTask(ImageView imageView, String path, int defaultImageResId, int type) {
            mImageView = imageView;
            mPath = path;
            mDefaultImageResId = defaultImageResId;
            mType = type;
            showDefaultImage();
        }

        public void setType(int type) {
            mType = type;
        }

        public void setPath(String path) {
            mPath = path;
        }

        public void setDefaultImageResId(int defaultImageResId) {
            mDefaultImageResId = defaultImageResId;
        }

        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        public void cancel() {
            mIsCancel = true;
        }

        @Override
        public void run() {
            if(mIsCancel) return;

            if(mType == THUMBNAIL_TYPE_IMAGE) {
                mBitmap = getImageThumbnail(mPath, mWidth, mHeight);
            } else if(mType == THUMBNAIL_TYPE_VIDEO) {
                mBitmap = getVideoThumbnail(mPath);
            }
            ThumbnailCache.saveBitmapCache(mPath, mBitmap);
            onFinish();
        }

        private void onFinish() {
            if(mIsCancel) return;

            Message finishMessage = Message.obtain();
            finishMessage.obj = this;
            finishMessage.what = MESSAGE_LOAD_FINISH;
            sThumbnailHandler.sendMessage(finishMessage);
        }

        private Bitmap getVideoThumbnail(String videoPath) {
            Bitmap bitmap = null;
            if(videoPath != null) {
                bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
            }
            return bitmap;
        }

        private Bitmap getImageThumbnail(String imagePath, int width, int height) {
            Bitmap bitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高，注意此处的bitmap为null
            BitmapFactory.decodeFile(imagePath, options);
            options.inJustDecodeBounds = false; // 设为 false
            // 计算缩放比
            int h = options.outHeight;
            int w = options.outWidth;
            int beWidth = w / width;
            int beHeight = h / height;
            int be;
            if(beWidth < beHeight) {
                be = beWidth;
            } else {
                be = beHeight;
            }
            if(be <= 0) {
                be = 1;
            }
            options.inSampleSize = be;
            // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            return bitmap;
        }

        private void showDefaultImage() {
            if(mImageView != null) {
                mImageView.setImageResource(mDefaultImageResId);
            }
        }
    }

    private static class LoadThumbnailHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj != null) {
                LoadThumbnailTask task = (LoadThumbnailTask)msg.obj;
                switch(msg.what) {
                    case MESSAGE_LOAD_START: {
                        showBitmapToView(task.mImageView, task.mDefaultImageResId);
                        break;
                    }
                    case MESSAGE_LOAD_FINISH: {
                        if(!task.mIsCancel) {
                            showBitmapToView(task.mImageView, task.mBitmap);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void showBitmapToView(ImageView view, Bitmap bitmap) {
        if(bitmap != null && view != null) {
            view.setImageBitmap(bitmap);
        }
    }

    private static void showBitmapToView(ImageView view, int resId) {
        if(view != null) {
            view.setImageResource(resId);
        }
    }
}
