package com.cxample.bookread.constant;

import com.cxample.bookread.R;

/**
 * Created by yanqing on 2018/5/23.
 */

public class ReadConfig {
    public static final int[] BG_COLORS = new int[]{
            R.color.white,
            R.color.cyan_blue_bg_color,
            R.color.brown_bg_color,
            R.color.green_bg_color,
            R.color.grey_900_bg_color
    };

    public static final int[] BG_COLOR_IMAGES = new int[]{
            R.drawable.read_bg_white,
            R.drawable.read_bg_blue,
            R.drawable.read_bg_brown,
            R.drawable.read_bg_green,
            R.drawable.read_bg_gray
    };

    public static final int[] TEXT_COLORS = new int[]{
            R.color.black,
            R.color.dark_blue_text_color,
            R.color.dark_brown_text_color,
            R.color.dark_green_text_color,
            R.color.dark_gray_text_color
    };

    public static final int TEXT_SIZE_COLUM = 0;
    public static final int TEXT_SIZE_TYPE_COLUM = 1;
    public static final int[][] TEXT_SIZE = {
            new int[]{R.dimen.text_13_size, 13},
            new int[]{R.dimen.text_15_size, 15},
            new int[]{R.dimen.text_16_size, 16},
            new int[]{R.dimen.text_19_size, 19},
            new int[]{R.dimen.text_22_size, 22},
            new int[]{R.dimen.text_25_size, 25},
            new int[]{R.dimen.text_28_size, 28}
    };
}
