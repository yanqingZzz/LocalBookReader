package com.cxample.bookread.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by yanqing on 2018/4/20.
 */
@Database(version = 1, entities = {Episode.class}, exportSchema = false)
public abstract class EpisodeDataBase extends RoomDatabase {
    private static final String NAME = "episode_db.db";
    private static final Object mLock = new Object();

    private static EpisodeDataBase sInstance;

    public abstract EpisodeDao episodeDao();

    public static EpisodeDataBase getInstance(Context context) {
        if(sInstance == null) {
            synchronized(mLock) {
                if(sInstance == null) {
                    sInstance = Room.databaseBuilder(context, EpisodeDataBase.class, NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return sInstance;
    }
}
