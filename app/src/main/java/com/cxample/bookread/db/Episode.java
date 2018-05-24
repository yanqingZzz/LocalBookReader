package com.cxample.bookread.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by yanqing on 2018/4/20.
 */

@Entity(tableName = "episode")
public class Episode {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;
    @ColumnInfo(name = "book_id")
    public int book_id;
    @ColumnInfo(name = "episode_index")
    public int episode_index;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "start_position")
    public int start_position;
    @ColumnInfo(name = "total_line")
    public int total_line;
}
