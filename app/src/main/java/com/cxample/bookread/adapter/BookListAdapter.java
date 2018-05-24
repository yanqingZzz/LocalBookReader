package com.cxample.bookread.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cxample.bookread.R;
import com.cxample.bookread.db.Book;
import com.cxample.bookread.utils.Utils;
import com.cxample.bookread.viewholder.AbsViewHolder;
import com.cxample.bookread.viewholder.AddBookViewHolder;
import com.cxample.bookread.viewholder.BookViewHolder;
import com.cxample.bookread.viewholder.EmptyViewHolder;
import com.cxample.bookread.viewholder.LoadingViewHolder;

import java.util.List;

/**
 * Created by yanqing on 2018/4/19.
 */

public class BookListAdapter extends RecyclerView.Adapter<AbsViewHolder> {
    public final static int STATUS_NONE = 0;
    public final static int STATUS_ADD = 1;

    private View.OnClickListener mItemClickListener;
    private View.OnClickListener mAddClickListener;

    private List<Book> mBooks;

    private Context mContext;

    public BookListAdapter(Context context) {
        mContext = context;
    }

    public void setBooks(List<Book> books) {
        mBooks = books;
        notifyDataSetChanged();
    }

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setAddClickListener(View.OnClickListener addClickListener) {
        mAddClickListener = addClickListener;
    }

    @Override
    public AbsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case STATUS_ADD: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.add_item_layout, parent, false);
                return new AddBookViewHolder(view);
            }
            case STATUS_NONE: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.book_item_layout, parent, false);
                return new BookViewHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(AbsViewHolder holder, int position) {
        switch(getItemViewType(position)) {
            case STATUS_NONE: {
                BookViewHolder viewHolder = (BookViewHolder)holder;
                Book book = mBooks.get(position);
                if(!TextUtils.isEmpty(book.icon_path)) {
                    //load image
                }
                viewHolder.mNameView.setText(book.name);
                viewHolder.mEpisodeView.setText(book.episode + "/" + book.episode_count);
                viewHolder.itemView.setTag(book);
                viewHolder.itemView.setOnClickListener(mItemClickListener);
                break;
            }
            case STATUS_ADD: {
                holder.itemView.setOnClickListener(mAddClickListener);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mBooks == null || mBooks.size() == 0 || position == mBooks.size()) return STATUS_ADD;
        return STATUS_NONE;
    }

    @Override
    public int getItemCount() {
        return mBooks == null ? 1 : mBooks.size() + 1;
    }
}
