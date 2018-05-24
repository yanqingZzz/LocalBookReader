package com.cxample.bookread.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by yanqing on 2018/4/19.
 */
@Entity(tableName = "book")
public class Book {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "episode")
    public int episode;

    @ColumnInfo(name = "page")
    public int page;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "time")
    public long timestamp;

    @ColumnInfo(name = "icon_path")
    public String icon_path;

    @ColumnInfo(name = "decode_type")
    public String decodeType;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "episode_count")
    public int episode_count;
}
