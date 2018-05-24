package com.cxample.bookread.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cxample.bookread.R;
import com.cxample.bookread.db.Episode;
import com.cxample.bookread.viewholder.AbsViewHolder;
import com.cxample.bookread.viewholder.CatalogViewHolder;

import java.util.List;

/**
 * Created by yanqing on 2018/5/23.
 */

public class CatalogAdapter extends RecyclerView.Adapter<AbsViewHolder> {
    private List<Episode> mEpisodes;
    private int mCurrentEpisode = -1;

    private View.OnClickListener mItemClickListener;

    private Context mContext;

    public CatalogAdapter(Context context) {
        mContext = context;
    }

    public void setEpisodes(List<Episode> episodes) {
        mEpisodes = episodes;
        notifyDataSetChanged();
    }

    public void setCurrentEpisode(int currentEpisode) {
        mCurrentEpisode = currentEpisode;
    }

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @Override
    public AbsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.catalog_item_layout, parent, false);
        return new CatalogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AbsViewHolder holder, int position) {
        CatalogViewHolder viewHolder = (CatalogViewHolder)holder;
        viewHolder.mTitle.setText(mEpisodes.get(position).title);

        if(position == (mCurrentEpisode - 1)) {
            viewHolder.mIcon.setImageResource(R.drawable.catalog_item_selected);
            viewHolder.mTitle.setTextColor(mContext.getResources().getColor(R.color.catalog_item_selected_color));
        } else {
            viewHolder.mIcon.setImageResource(R.drawable.catalog_item_unselected);
            viewHolder.mTitle.setTextColor(mContext.getResources().getColor(R.color.catalog_item_unselected_color));
        }

        viewHolder.itemView.setTag(mEpisodes.get(position));
        viewHolder.itemView.setOnClickListener(mItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mEpisodes == null ? 0 : mEpisodes.size();
    }
}
