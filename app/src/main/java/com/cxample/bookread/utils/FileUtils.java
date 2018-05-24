package com.cxample.bookread.utils;

import android.content.Context;
import android.util.Log;

import com.cxample.bookread.MainActivity;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.db.Episode;
import com.cxample.bookread.db.EpisodeDataBase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by yanqing on 2018/4/11.
 */

public class FileUtils {
    private static final String[] TEXT = new String[]{
            "txt"
    };

    private static final String[] VIDEO = new String[]{
            "mpeg",
            "mp4",
            "avi",
            "wmv",
            "3gp",
            "mkv",
            "rmvb",
            "mov"
    };

    private static final String[] MUSIC = new String[]{
            "wave",
            "mp3",
            "wma",
            "flac",
            "aac",
            "ape",
            "amr",
            "vqf",
            "aiff"
    };

    private static final String[] IMAGE = new String[]{
            "jpg",
            "bmp",
            "png",
            "tiff",
            "gif",
            "webp"
    };

    public static boolean isText(String suffix) {
        for(String s : TEXT) {
            if(s.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVideo(String suffix) {
        for(String s : VIDEO) {
            if(s.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMusic(String suffix) {
        for(String s : MUSIC) {
            if(s.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isImage(String suffix) {
        for(String s : IMAGE) {
            if(s.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static String getTxtCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if(read == -1)
                return charset;
            if(first3Bytes[0] == (byte)0xFF && first3Bytes[1] == (byte)0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if(first3Bytes[0] == (byte)0xFE && first3Bytes[1]
                    == (byte)0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if(first3Bytes[0] == (byte)0xEF && first3Bytes[1]
                    == (byte)0xBB
                    && first3Bytes[2] == (byte)0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if(!checked) {
                int loc = 0;
                while((read = bis.read()) != -1) {
                    loc++;
                    if(read >= 0xF0)
                        break;
                    //单独出现BF以下的，也算是GBK
                    if(0x80 <= read && read <= 0xBF)
                        break;
                    if(0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if(0x80 <= read && read <= 0xBF)// 双字节 (0xC0 - 0xDF)
                            // (0x80 -
                            // 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                        // 也有可能出错，但是几率较小
                    } else if(0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if(0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if(0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
                System.out.println(loc + " " + Integer.toHexString(read));
            }
            bis.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    //获取指定分集的内容
    public static ArrayList<String> getEpisodeContent(Context context, Book book, int episodeIndex) {
        ArrayList<String> episodeContent = new ArrayList<>();
        Episode episode = EpisodeDataBase.getInstance(context.getApplicationContext())
                .episodeDao().getEpisodeByIndex(book.id, episodeIndex);
        if(episode == null) {
            return episodeContent;
        }
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(book.path);
            isr = new InputStreamReader(fis, book.decodeType);
            br = new BufferedReader(isr);
            if(br.skip(episode.start_position) > 0) {
                StringBuilder paragraph = new StringBuilder();
                String content = br.readLine();
                for(int i = 1; content != null && i <= episode.total_line; i++) {
                    //全角(非全角)的两个空格开头
                    if(content.startsWith("　　")
                            ||content.startsWith("  ")) {
                        episodeContent.add(paragraph.toString());
                        paragraph = new StringBuilder();
                    }
                    paragraph.append(content);

                    content = br.readLine();
                }
                episodeContent.add(paragraph.toString());
            }

        } catch(IOException e) {
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

            } catch(Exception ex) {

            }
        }
        return episodeContent;
    }

}
