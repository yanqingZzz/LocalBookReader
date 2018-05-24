package com.cxample.bookread.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yanqing on 2018/5/23.
 */

public class SharePreferenceUtils {
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("read_setting", Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static void saveReadBackgroundColor(Context context, int colorType) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt("read_bg", colorType);
        editor.commit();
    }

    public static int getReadBackgroundColor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getInt("read_bg", 0);
    }

    public static void saveIsNightMode(Context context,boolean isNightMode){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean("night_mode", isNightMode);
        editor.commit();
    }

    public static boolean getIsNightMode(Context context) {
        return getSharedPreferences(context).getBoolean("night_mode", false);
    }

    public static void saveScreenLight(Context context,int light){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt("screen_light", light);
        editor.commit();
    }

    public static int getScreenLight(Context context){
        return getSharedPreferences(context).getInt("screen_light", -1);
    }

    public static void setFollowSystemLight(Context context, boolean followSystem) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean("light_follow_system", followSystem);
        editor.commit();
    }

    public static boolean getFollowSystemLight(Context context) {
        return getSharedPreferences(context).getBoolean("light_follow_system", true);
    }

    public static void saveTextSize(Context context, int type) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt("text_size", type);
        editor.commit();
    }

    public static int getTextSize(Context context) {
        return getSharedPreferences(context).getInt("text_size", 3);
    }
}
