package com.cxample.bookread.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by yanqing on 2018/4/19.
 */

@Database(version = 1, entities = {Book.class}, exportSchema = false)
public abstract class BookDataBase extends RoomDatabase {
    private static final String NAME = "book_db.db";
    private static final Object mLock = new Object();
    private static BookDataBase sInstance;

    public abstract BookDao bookDao();

    public static BookDataBase getInstance(Context context) {
        if(sInstance == null) {
            synchronized(mLock) {
                if(sInstance == null) {
                    sInstance = Room.databaseBuilder(context, BookDataBase.class, NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return sInstance;
    }
}
