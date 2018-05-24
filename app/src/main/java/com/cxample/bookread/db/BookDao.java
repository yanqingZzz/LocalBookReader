package com.cxample.bookread.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by yanqing on 2018/4/19.
 */

@Dao
public interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Book book);

    @Delete
    void delete(Book book);

    @Update
    void update(Book book);

    @Query("SELECT * FROM book ORDER BY time DESC")
    List<Book> getAll();

    @Query("SELECT * FROM book WHERE id = :id")
    Book getBook(int id);

    @Query("SELECT * FROM book WHERE path = :path")
    Book getBookByPath(String path);

    @Query("DELETE FROM book WHERE id = :id")
    void deleteById(int id);
}
