package com.cxample.bookread.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanqing on 2018/4/19.
 */

public class BookListAdapter extends RecyclerView.Adapter<AbsViewHolder> {
    public final static int STATUS_NONE = 0;
    public final static int STATUS_ADD = 1;

    private View.OnClickListener mItemClickListener;
    private View.OnLongClickListener mItemLongClickListener;
    private View.OnClickListener mAddClickListener;

    private SparseBooleanArray mBooleanArray;

    private List<Book> mBooks;

    private Context mContext;

    private boolean isSelectMode = false;

    public BookListAdapter(Context context) {
        mContext = context;
        mBooleanArray = new SparseBooleanArray();
    }

    public void setBooks(List<Book> books) {
        mBooks = books;
        notifyDataSetChanged();
    }

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        mItemLongClickListener = itemLongClickListener;
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
                viewHolder.itemView.setOnLongClickListener(mItemLongClickListener);
                if(isSelectMode) {
                    viewHolder.mSelectStatusView.setVisibility(View.VISIBLE);
                    viewHolder.mSelectStatusView.setSelected(getItemSelected(book.id));
                    viewHolder.itemView.setOnClickListener(mItemSelectedClickListener);
                } else {
                    viewHolder.mSelectStatusView.setVisibility(View.GONE);
                    viewHolder.itemView.setOnClickListener(mItemClickListener);
                }
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

    private View.OnClickListener mItemSelectedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Book book = (Book)v.getTag();
            boolean isSelected = getItemSelected(book.id);
            setItemSelected(book.id, !isSelected);
            notifyDataSetChanged();
        }
    };

    private void setItemSelected(int id, boolean selected) {
        if(mBooleanArray == null) mBooleanArray = new SparseBooleanArray();
        mBooleanArray.put(id, selected);
    }

    private boolean getItemSelected(int id) {
        if(mBooleanArray == null) return false;
        return mBooleanArray.get(id, false);
    }

    public void selectAll(boolean isSelected) {
        if(mBooks != null) {
            for(Book book : mBooks) {
                setItemSelected(book.id, isSelected);
            }
            notifyDataSetChanged();
        }
    }

    public ArrayList<Book> getAllSelected() {
        ArrayList<Book> selectedBooks = new ArrayList<>();
        if(mBooks != null) {
            for(Book book : mBooks) {
                if(getItemSelected(book.id)) {
                    selectedBooks.add(book);
                }
            }
        }
        return selectedBooks;
    }

    public void showSelectMode(int id) {
        setItemSelected(id, true);
        isSelectMode = true;
        notifyDataSetChanged();
    }

    public void hideSelectMode() {
        selectAll(false);
        isSelectMode = false;
        notifyDataSetChanged();
    }
}
