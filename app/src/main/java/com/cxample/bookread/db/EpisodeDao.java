package com.cxample.bookread.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by yanqing on 2018/4/20.
 */
@Dao
public interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Episode episode);

    @Query("SELECT * FROM episode")
    List<Episode> getAll();

    @Query("DELETE FROM episode WHERE book_id = :bookId")
    void deleteByBookId(int bookId);

    @Query("SELECT * FROM episode WHERE book_id = :bookId AND episode_index = :index")
    Episode getEpisodeByIndex(int bookId, int index);

    @Query("SELECT * FROM episode WHERE book_id = :bookId")
    List<Episode> getEpisodesByBookId(int bookId);
}
