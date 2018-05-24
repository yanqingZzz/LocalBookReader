package com.cxample.bookread.model;

import java.util.Comparator;

/**
 * Created by yanqing on 2018/4/11.
 */

public class FileItem implements Comparator<FileItem> {
    public static final int FILE_TYPE_DIRECTORY = 0;
    public static final int FILE_TYPE_TEXT = 1;
    public static final int FILE_TYPE_VIDEO = 2;
    public static final int FILE_TYPE_MUSIC = 3;
    public static final int FILE_TYPE_IMAGE = 4;
    public static final int FILE_TYPE_UNKNOWN = 5;

    public int index;
    public String path;
    public String name;
    public String suffix;
    public int type;
    public int childCount;
    public int fileSize;
    public long lastModifyTime;

    @Override
    public int compare(FileItem o1, FileItem o2) {
        return o1.name.compareToIgnoreCase(o2.name);
    }
}
