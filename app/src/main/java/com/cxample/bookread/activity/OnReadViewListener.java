package com.cxample.bookread.activity;

/**
 * Created by yanqing on 2018/5/22.
 */

public interface OnReadViewListener {
    void onGetCurrent(int episode, boolean success);

    void onGetNext(int episode, boolean success);

    void onGetPrevious(int episode, boolean success);

    void onShowMenu();

    boolean menuOnShow();
}
