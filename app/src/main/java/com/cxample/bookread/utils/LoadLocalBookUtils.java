package com.cxample.bookread.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.BookDao;
import com.cxample.bookread.db.BookDataBase;
import com.cxample.bookread.db.Episode;
import com.cxample.bookread.db.EpisodeDao;
import com.cxample.bookread.db.EpisodeDataBase;
import com.cxample.bookread.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yanqing on 2018/4/19.
 */

public class LoadLocalBookUtils {
    private static final String MATCHES_EPISODE_TITLE = "第([0-9]|[零,一,二,三,四,五,六,七,八,九,十,百,千,万,亿])+章.*";
    private static final String MATCHES_AUTHOR = "作者[:,：].*";

    private static final int HANDLER_MESSAGE_START = 0;
    private static final int HANDLER_MESSAGE_END = 1;
    private static final int HANDLER_MESSAGE_ERROR = 2;
    private static MyHandler sHandler = new MyHandler();

    public static void loadBook(final Context context, final String path, final OnLoadLocalBookListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(listener != null) {
                    Message message = Message.obtain();
                    message.what = HANDLER_MESSAGE_START;
                    message.obj = listener;
                    sHandler.sendMessage(message);
                }
                boolean isHeader = true;


                EpisodeDao episodeDao = EpisodeDataBase.getInstance(context.getApplicationContext()).episodeDao();
                BookDao bookDao = BookDataBase.getInstance(context.getApplicationContext()).bookDao();

                Book book = bookDao.getBookByPath(path);
                if(book != null) {
                    episodeDao.deleteByBookId(book.id);
                } else {
                    book = createNewBook(path);
                    bookDao.insert(book);
                    book = bookDao.getBookByPath(path);
                }

                FileInputStream fis = null;
                InputStreamReader isr = null;
                BufferedReader br = null;
                try {
                    fis = new FileInputStream(path);
                    isr = new InputStreamReader(fis, book.decodeType);
                    br = new BufferedReader(isr);
                    int episode_total_line = 0;
                    int episode_start_position = 0;
                    int episode_count = 0;
                    Episode lastEpisode = null;

                    String content = br.readLine();
                    while(content != null) {
                        if(isHeader && matchesAuthor(content)) {
                            book.author = getAuthor(content);
                        } else if(matchesEpisodeTitle(content)) {
                            isHeader = false;

                            if(lastEpisode != null) {
                                lastEpisode.total_line = episode_total_line;
                                episodeDao.insert(lastEpisode);
                            }

                            episode_total_line = 0;
                            episode_count++;

                            Episode episode = new Episode();
                            episode.book_id = book.id;
                            episode.title = content;
                            episode.episode_index = episode_count;
                            episode.start_position = episode_start_position;

                            lastEpisode = episode;
                        }

                        episode_start_position += content.length() + 2;
                        content = br.readLine();
                        episode_total_line++;
                    }
                    if(lastEpisode != null) {
                        lastEpisode.total_line = episode_total_line;
                        episodeDao.insert(lastEpisode);
                    }
                    book.episode_count = episode_count;
                    if(book.episode == 0) {
                        book.episode = 1;
                    }
                    bookDao.update(book);
                    if(listener != null) {
                        Message message = Message.obtain();
                        message.what = HANDLER_MESSAGE_END;
                        message.obj = listener;
                        sHandler.sendMessage(message);
                    }
                } catch(IOException e) {
                    if(listener != null) {
                        Message message = Message.obtain();
                        message.what = HANDLER_MESSAGE_ERROR;
                        message.obj = listener;
                        sHandler.sendMessage(message);
                    }
                } finally {
                    try {
                        if(fis != null) {
                            fis.close();
                        }
                        if(isr != null) {
                            isr.close();
                        }
                        if(br != null) {
                            br.close();
                        }

                    } catch(Exception e) {

                    }
                }

            }
        }).start();
    }

    private static Book createNewBook(String path) {
        if(!checkBook(path)) throw new IllegalArgumentException("文件不存在");
        File file = new File(path);
        Book book = new Book();
        book.path = path;
        book.name = getBookName(file.getName());
        book.episode = 0;
        book.page = 0;
        book.icon_path = null;
        book.timestamp = System.currentTimeMillis();
        book.decodeType = FileUtils.getTxtCharset(file);
        return book;
    }

    private static String getAuthor(String text) {
        text = text.replaceAll(" ", "");
        String[] arr = text.split("[:,：]");
        if(arr.length < 2) {
            return "";
        } else {
            return arr[1];
        }
    }

    private static boolean matchesEpisodeTitle(String text) {
        return text.matches(MATCHES_EPISODE_TITLE);
    }

    private static boolean matchesAuthor(String text) {
        return text.matches(MATCHES_AUTHOR);
    }

    private static class MyHandler extends Handler {
        public MyHandler() {}

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.obj != null && msg.obj instanceof OnLoadLocalBookListener) {
                OnLoadLocalBookListener listener = (OnLoadLocalBookListener)msg.obj;
                switch(msg.what) {
                    case HANDLER_MESSAGE_START: {
                        listener.onStart();
                        break;
                    }
                    case HANDLER_MESSAGE_END: {
                        listener.onEnd();
                        break;
                    }
                    case HANDLER_MESSAGE_ERROR: {
                        listener.onError();
                        break;
                    }
                }
            }
        }
    }


    private static String getBookName(String name) {
        int index = name.lastIndexOf(".");
        if(index < 0) {
            return name;
        } else {
            return name.substring(0, index);
        }
    }

    private static boolean checkBook(String bookPath) {
        if(bookPath == null) return false;
        File file = new File(bookPath);
        return file.exists() && file.isFile();
    }

    public interface OnLoadLocalBookListener {
        void onStart();

        void onEnd();

        void onError();
    }

}
