package com.cxample.bookread.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxample.bookread.R;

/**
 * Created by yanqing on 2018/4/11.
 */

public class BookViewHolder extends AbsViewHolder {
    public ImageView mIconView;
    public TextView mNameView;
    public TextView mEpisodeView;
    public ImageView mSelectStatusView;

    public BookViewHolder(View itemView) {
        super(itemView);
        mIconView = itemView.findViewById(R.id.icon);
        mNameView = itemView.findViewById(R.id.name);
        mEpisodeView = itemView.findViewById(R.id.episode);
        mSelectStatusView = itemView.findViewById(R.id.item_select_status);
    }
}
