package com.cxample.bookread.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cxample.bookread.R;

/**
 * Created by yanqing on 2018/5/23.
 */

public class CatalogViewHolder extends AbsViewHolder {
    public ImageView mIcon;
    public TextView mTitle;

    public CatalogViewHolder(View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.item_icon);
        mTitle = itemView.findViewById(R.id.item_title);
    }
}
