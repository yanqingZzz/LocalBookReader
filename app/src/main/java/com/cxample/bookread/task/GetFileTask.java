package com.cxample.bookread.task;

import android.os.AsyncTask;

import com.cxample.bookread.model.FileItem;
import com.cxample.bookread.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by yanqing on 2018/4/11.
 */

public class GetFileTask extends AsyncTask<String, Void, ArrayList<FileItem>> {
    private OnGetFileListener mListener;

    public GetFileTask(OnGetFileListener listener) {
        mListener = listener;
    }

    protected void onPreExecute() {
        if(mListener != null) {
            mListener.onPreExecute();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<FileItem> fileItems) {
        if(mListener != null) {
            mListener.onPostExecute(fileItems);
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }

    @Override
    protected ArrayList<FileItem> doInBackground(String... strings) {
        ArrayList<FileItem> fileItems = null;
        ArrayList<FileItem> directoryItems = null;
        if(strings != null && strings.length > 0) {
            File file = new File(strings[0]);
            if(file.exists()) {
                fileItems = new ArrayList<>();
                directoryItems = new ArrayList<>();
                File[] files = file.listFiles();
                if(files != null) {
                    for(File f : files) {
                        if(f.exists() && f.getName().indexOf(".") != 0) {
                            FileItem item = new FileItem();
                            item.name = f.getName();
                            item.suffix = getSuffix(item.name);
                            item.path = f.getAbsolutePath();
                            item.lastModifyTime = f.lastModified();
                            item.type = getFileType(f);
                            if(f.isDirectory()) {
                                File[] tempFiles = f.listFiles();
                                item.childCount = tempFiles == null ? 0 : tempFiles.length;
                                directoryItems.add(item);
                            } else {
                                item.fileSize = getFileSize(f);
                                fileItems.add(item);
                            }
                        }
                    }
                }
            }
        }
        return sort(directoryItems, fileItems);
    }

    private ArrayList<FileItem> sort(ArrayList<FileItem> directoryItems, ArrayList<FileItem> fileItems) {
        ArrayList<FileItem> result = new ArrayList<>();
        if(directoryItems != null && directoryItems.size() > 0) {
            Collections.sort(directoryItems, new FileItem());
            result.addAll(directoryItems);
        }
        if(fileItems != null && fileItems.size() > 0) {
            Collections.sort(fileItems, new FileItem());
            result.addAll(fileItems);
        }
        return result;
    }

    private String getSuffix(String name) {
        int index = name.lastIndexOf(".");
        if(index > 0) {
            String[] names = name.split("\\.");
            if(names.length > 1) {
                return names[names.length - 1];
            }
        }
        return null;
    }

    private int getFileType(File file) {
        if(file.isDirectory()) {
            return FileItem.FILE_TYPE_DIRECTORY;
        } else {
            String suffix = getSuffix(file.getName());
            if(suffix != null) {
                if(FileUtils.isText(suffix)) {
                    return FileItem.FILE_TYPE_TEXT;
                } else if(FileUtils.isVideo(suffix)) {
                    return FileItem.FILE_TYPE_VIDEO;
                } else if(FileUtils.isMusic(suffix)) {
                    return FileItem.FILE_TYPE_MUSIC;
                } else if(FileUtils.isImage(suffix)) {
                    return FileItem.FILE_TYPE_IMAGE;
                }
            }
        }
        return FileItem.FILE_TYPE_UNKNOWN;
    }

    private int getFileSize(File file) {
        int size = 0;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            size = inputStream.available();
            inputStream.close();
        } catch(Exception e) {
        }
        return size;
    }

    public interface OnGetFileListener {
        void onPreExecute();

        void onPostExecute(ArrayList<FileItem> fileItems);

    }
}
