package com.cxample.bookread.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cxample.bookread.R;
import com.cxample.bookread.model.FileItem;
import com.cxample.bookread.task.GetFileTask;
import com.cxample.bookread.thunbnail.ThumbnailLoader;
import com.cxample.bookread.utils.Utils;
import com.cxample.bookread.viewholder.AbsViewHolder;
import com.cxample.bookread.viewholder.EmptyViewHolder;
import com.cxample.bookread.viewholder.FileViewHolder;
import com.cxample.bookread.viewholder.LoadingViewHolder;

import pub.devrel.easypermissions.EasyPermissions;

public class FileBrowserActivity extends ToolbarActivity implements EasyPermissions.PermissionCallbacks {
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private TextView mPathView;
    private LinearLayout mEditFunctionLayout;
    private TextView mEditSelectedAllView;
    private TextView mEditDeleteView;
    private TextView mEditCancelView;

    private String mDefaultPath;
    private String mCurrentPath;

    private int mSelectedPosition;

    private Animation mEditAnimationIn;
    private Animation mEditAnimationOut;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        setContentView(R.layout.activity_file_brower);
        setTitle(R.string.app_name);

        mIntent = getIntent();
        if(mIntent == null) {
            mIntent = new Intent();
        }
        setResult(RESULT_CANCELED);

        mEditFunctionLayout = findViewById(R.id.edit_function);
        mEditSelectedAllView = findViewById(R.id.edit_selected_all);
        mEditDeleteView = findViewById(R.id.edit_delete);
        mEditCancelView = findViewById(R.id.edit_cancel);
        mEditSelectedAllView.setOnClickListener(mEditSelectedAllClickListener);
        mEditDeleteView.setOnClickListener(mEditDeleteClickListener);
        mEditCancelView.setOnClickListener(mEditCancelClickListener);

        mPathView = findViewById(R.id.current_path);

        mRecyclerView = findViewById(R.id.recycler_list);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAdapter = new FileAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mDefaultPath = Environment.getExternalStorageDirectory().getPath();
        getFileData(mDefaultPath);
    }

    public void onRefresh(View view) {
        getFileData(mCurrentPath);
    }

    private void getFileData(String path) {
        mCurrentPath = path;
        mPathView.setText(path);
        if(checkReadPermission()) {
            new GetFileTask(new GetFileTask.OnGetFileListener() {
                @Override
                public void onPreExecute() {
                    mAdapter.setStatus(FileAdapter.STATUS_LOADING);
                    mSelectedPosition = mLayoutManager.findFirstVisibleItemPosition();
                }

                @Override
                public void onPostExecute(ArrayList<FileItem> fileItems) {
                    mAdapter.setFileItems(fileItems);
                    if(fileItems != null) {
                        if(mSelectedPosition >= fileItems.size()) {
                            mSelectedPosition = fileItems.size() - 1;
                        } else if(mSelectedPosition < 0) {
                            mSelectedPosition = 0;
                        }
                        mLayoutManager.scrollToPosition(mSelectedPosition);
                    }
                }
            }).execute(path);
        }
    }

    private void showEditModel() {
        mAdapter.setEditModel(true);
        mPathView.setText("已选中 0 项");
        if(mEditAnimationIn == null) {
            mEditAnimationIn = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        }
        mEditFunctionLayout.clearAnimation();
        mEditFunctionLayout.setVisibility(View.VISIBLE);
        mEditFunctionLayout.startAnimation(mEditAnimationIn);
    }

    private void hideEditEditModel() {
        mAdapter.setEditModel(false);
        mPathView.setText(mCurrentPath);
        if(mEditAnimationOut == null) {
            mEditAnimationOut = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
            mEditAnimationOut.setAnimationListener(mAnimationOutListener);
        }
        mEditFunctionLayout.clearAnimation();
        mEditFunctionLayout.startAnimation(mEditAnimationOut);
    }

    private void updateDeleteButtonState(int selectedCount) {
        mPathView.setText("已选中 " + selectedCount + " 项");
        if(selectedCount > 0) {
            mEditDeleteView.setTextColor(Color.BLACK);
            mEditDeleteView.setClickable(true);
        } else {
            mEditDeleteView.setTextColor(Color.GRAY);
            mEditDeleteView.setClickable(false);
        }
    }

    private Animation.AnimationListener mAnimationOutListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mEditFunctionLayout.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private View.OnClickListener mEditSelectedAllClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.isSelected()) {
                mAdapter.setAllSelected(false);
                v.setSelected(false);
                ((TextView)v).setText("全选");
            } else {
                mAdapter.setAllSelected(true);
                v.setSelected(true);
                ((TextView)v).setText("取消全选");
            }
        }
    };

    private View.OnClickListener mEditDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<FileItem> fileItems = mAdapter.getSelectedItems();
            delete(fileItems.toArray(new FileItem[fileItems.size()]));
        }
    };

    private View.OnClickListener mEditCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideEditEditModel();
        }
    };

    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object o = v.getTag();
            if(o != null) {
                FileItem item = (FileItem)o;
                switch(item.type) {
                    case FileItem.FILE_TYPE_DIRECTORY: {
                        mSelectedPosition = 0;
                        getFileData(item.path);
                        break;
                    }
                    case FileItem.FILE_TYPE_IMAGE: {
                        showFile(item.path, "image/*");
                        break;
                    }
                    case FileItem.FILE_TYPE_MUSIC: {
                        showFile(item.path, "audio/*");
                        break;
                    }
                    case FileItem.FILE_TYPE_TEXT: {
//                        showFile(item.path, "text/*");
                        openTxtFile(item.path, item.suffix);
                        break;
                    }
                    case FileItem.FILE_TYPE_VIDEO: {
                        showFile(item.path, "video/*");
                        break;
                    }
                    default: {
                        Toast.makeText(FileBrowserActivity.this, "未知类型文件", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    private void openTxtFile(final String path, String suffix) {
        if(path == null || suffix == null || !suffix.equalsIgnoreCase("txt")) return;
        File file = new File(path);
        if(file.exists() && file.isFile()) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定打开" + file.getName() + "?")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mIntent.putExtra("path", path);
                            setResult(RESULT_OK, mIntent);
                            finish();
                        }
                    }).create().show();
        }
    }

    private void showFile(String path, String type) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(Uri.fromFile(new File(path)), type);
//        startActivity(intent);
    }


//    private View.OnCreateContextMenuListener mContextMenuListener = new View.OnCreateContextMenuListener() {
//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            FileItem item = (FileItem)v.getTag();
//            mCurMenuItemIndex = item.index;
//            getMenuInflater().inflate(R.menu.file_item_menu, menu);
//        }
//    };

    private View.OnClickListener mEditModelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int)v.getTag();
            mAdapter.setReverseSelected(position);
        }
    };

    private class FileAdapter extends RecyclerView.Adapter<AbsViewHolder> {
        private final static int STATUS_NONE = 0;
        private final static int STATUS_LOADING = 1;
        private final static int STATUS_EMPTY = 2;

        private int mStatus;
        private ArrayList<FileItem> mFileItems;

        private SparseBooleanArray mBooleanArray;
        private boolean mIsEditModel = false;

        public FileAdapter() {
            mBooleanArray = new SparseBooleanArray();
        }

        public void setFileItems(ArrayList<FileItem> fileItems) {
            mFileItems = fileItems;
            if(fileItems == null || fileItems.size() == 0) {
                mStatus = STATUS_EMPTY;
            } else {
                mStatus = STATUS_NONE;
            }
            notifyDataSetChanged();
        }

        public void setStatus(int status) {
            mFileItems = null;
            mStatus = status;
            notifyDataSetChanged();
        }

        public void setEditModel(boolean editModel) {
            mIsEditModel = editModel;
            mBooleanArray.clear();
            notifyDataSetChanged();
            if(editModel) {
                updateDeleteButtonState(getSelectedCount());
            }
        }

        private boolean getSelected(int position) {
            return mBooleanArray.get(position, false);
        }

        private void setSelected(int position, boolean selected) {
            mBooleanArray.put(position, selected);
            notifyDataSetChanged();
            updateDeleteButtonState(getSelectedCount());
        }

        private int getSelectedCount() {
            int count = 0;
            if(mFileItems != null) {
                for(int i = 0; i < mFileItems.size(); i++) {
                    if(getSelected(i)) {
                        count++;
                    }
                }
            }
            return count;
        }

        public void setReverseSelected(int position) {
            setSelected(position, !getSelected(position));
        }

        public void setAllSelected(boolean isSelected) {
            if(mFileItems != null) {
                for(int i = 0; i < mFileItems.size(); i++) {
                    setSelected(i, isSelected);
                }
            }
        }

        public ArrayList<FileItem> getSelectedItems() {
            ArrayList<FileItem> result = new ArrayList<>();
            if(mFileItems != null) {
                for(int i = 0; i < mFileItems.size(); i++) {
                    if(getSelected(i)) {
                        result.add(mFileItems.get(i));
                    }
                }
            }
            return result;
        }

        @Override
        public AbsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch(viewType) {
                case STATUS_EMPTY: {
                    View view = LayoutInflater.from(FileBrowserActivity.this).inflate(R.layout.empty_layout, parent, false);
                    return new EmptyViewHolder(view);
                }
                case STATUS_LOADING: {
                    View view = LayoutInflater.from(FileBrowserActivity.this).inflate(R.layout.loading_layout, parent, false);
                    return new LoadingViewHolder(view);
                }
                case STATUS_NONE: {
                    View view = LayoutInflater.from(FileBrowserActivity.this).inflate(R.layout.item_layout, parent, false);
                    return new FileViewHolder(view);
                }
            }
            return null;
        }

        @Override
        public void onBindViewHolder(AbsViewHolder holder, int position) {
            switch(getItemViewType(position)) {
                case STATUS_NONE: {
                    FileViewHolder viewHolder = (FileViewHolder)holder;
                    FileItem item = mFileItems.get(position);
                    item.index = position;
                    switch(item.type) {
                        case FileItem.FILE_TYPE_TEXT: {
                            viewHolder.mIconView.setImageResource(R.drawable.ic_txt);
                            break;
                        }
                        case FileItem.FILE_TYPE_DIRECTORY: {
                            viewHolder.mIconView.setImageResource(R.drawable.ic_folder);
                            break;
                        }
                        case FileItem.FILE_TYPE_IMAGE: {
                            //显示缩略图
                            ThumbnailLoader.loadImageThumbnail(item.path, viewHolder.mIconView, R.drawable.ic_image);
                            break;
                        }
                        case FileItem.FILE_TYPE_MUSIC: {
                            viewHolder.mIconView.setImageResource(R.drawable.ic_music);
                            break;
                        }
                        case FileItem.FILE_TYPE_VIDEO: {
                            //显示缩略图
                            ThumbnailLoader.LoadVideoThumbnail(item.path, viewHolder.mIconView, R.drawable.ic_video);
                            break;
                        }
                        case FileItem.FILE_TYPE_UNKNOWN: {
                            viewHolder.mIconView.setImageResource(R.drawable.ic_unknown);
                            break;
                        }

                    }
                    viewHolder.mNameView.setText(item.name);
                    if(item.type != FileItem.FILE_TYPE_DIRECTORY) {
                        viewHolder.mChildCountView.setText(Utils.changeFileSizeToString(FileBrowserActivity.this, item.fileSize));
                    } else {
                        viewHolder.mChildCountView.setText(item.childCount + "项");
                    }
                    viewHolder.mModifyTimeView.setText(Utils.changeTimeToString(item.lastModifyTime));
                    if(mIsEditModel) {
                        viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                        viewHolder.mCheckBox.setChecked(getSelected(position));
                        viewHolder.itemView.setTag(position);
                        viewHolder.itemView.setOnClickListener(mEditModelClickListener);
                    } else {
                        viewHolder.mCheckBox.setVisibility(View.GONE);
                        viewHolder.itemView.setTag(item);
                        viewHolder.itemView.setOnClickListener(mItemClickListener);
//                        viewHolder.itemView.setOnCreateContextMenuListener(mContextMenuListener);
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemCount() {
            if(mStatus == STATUS_NONE) {
                return mFileItems == null ? 0 : mFileItems.size();
            }
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return mStatus;
        }

        public FileItem getFileItem(int position) {
            if(mFileItems != null && position >= 0 && position < mFileItems.size()) {
                return mFileItems.get(position);
            }
            return null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public static final String[] WRITE_READ_EXTERNAL_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    protected boolean checkReadPermission() {
        if(!EasyPermissions.hasPermissions(this, WRITE_READ_EXTERNAL_PERMISSION)) {
            EasyPermissions.requestPermissions(this, "需要读写权限",
                    100, WRITE_READ_EXTERNAL_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getFileData(mCurrentPath);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mEditFunctionLayout.getVisibility() == View.VISIBLE) {
                hideEditEditModel();
                return true;
            }
            if(!mCurrentPath.equals(mDefaultPath)) {
                File file = new File(mCurrentPath);
                if(file.exists()) {
                    File parentFile = file.getParentFile();
                    if(parentFile != null && parentFile.exists()) {
                        mSelectedPosition = 0;
                        getFileData(parentFile.getAbsolutePath());
                        return true;
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.file_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.add) {
//            newDirectoryDialog();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    private int mCurMenuItemIndex;
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.delete) {
//            delete(mAdapter.getFileItem(mCurMenuItemIndex));
//        } else if(item.getItemId() == R.id.rename) {
//            rename(mAdapter.getFileItem(mCurMenuItemIndex));
//        } else if(item.getItemId() == R.id.edit) {
//            showEditModel();
//        }
//        return super.onContextItemSelected(item);
//    }

    private void newDirectoryDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_directory_layout, null);
        final EditText editText = view.findViewById(R.id.input);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新建文件夹");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                if(!TextUtils.isEmpty(name)) {
                    String path = mCurrentPath + "/" + name;
                    File file = new File(path);
                    if(file.exists()) {
                        Toast.makeText(FileBrowserActivity.this, "文件夹已存在", Toast.LENGTH_SHORT).show();
                    } else {
                        if(file.mkdir()) {
                            getFileData(mCurrentPath);
                            Toast.makeText(FileBrowserActivity.this, "新建文件夹成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FileBrowserActivity.this, "新建文件夹失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(FileBrowserActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void delete(final FileItem... item) {
        if(item != null && item.length > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(getDeleteHint(item))
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.setEditModel(false);
                            mAdapter.setStatus(FileAdapter.STATUS_LOADING);
                            hideEditEditModel();
                            for(FileItem fileItem : item) {
                                File file = new File(fileItem.path);
                                if(file.exists() && !deleteFile(file)) {
                                    Toast.makeText(FileBrowserActivity.this, "删除" + fileItem.name + "失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                            Toast.makeText(FileBrowserActivity.this, "删除完成", Toast.LENGTH_SHORT).show();
                            getFileData(mCurrentPath);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }
    }

    //递归删除文件
    private boolean deleteFile(File file) {
        if(file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            if(files != null && files.length > 0) {
                for(File file1 : files) {
                    if(!deleteFile(file1)) {
                        return false;
                    }
                }
            }
            return file.delete();
        }
    }

    private String getDeleteHint(FileItem... fileItems) {
        boolean hasFile = false;
        boolean hasFolder = false;
        if(fileItems.length == 1) {
            File file = new File(fileItems[0].path);
            if(file.isFile()) {
                return "确定删除选中的文件吗?";
            }
            return "确定删除选中的文件夹吗?";
        } else {
            for(FileItem item : fileItems) {
                File file = new File(item.path);
                if(file.isFile()) {
                    hasFile = true;
                } else {
                    hasFolder = true;
                }
            }
            if(hasFile && hasFolder) {
                return "确定删除选中的所有文件和文件夹吗?";
            } else if(hasFile) {
                return "确定删除选中的所有文件吗?";
            } else {
                return "确定删除选中的所有文件夹吗?";
            }
        }
    }

    private void rename(FileItem item) {
        if(item != null) {
            final File file = new File(item.path);
            if(file.exists()) {
                View view = LayoutInflater.from(this).inflate(R.layout.add_directory_layout, null);
                final EditText editText = view.findViewById(R.id.input);
                editText.setText(item.name);
                editText.setSelection(item.name.length());
                new AlertDialog.Builder(this)
                        .setTitle("重命名")
                        .setView(view)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = editText.getText().toString();
                                if(!TextUtils.isEmpty(name)) {
                                    String path = mCurrentPath + "/" + name;
                                    File newFile = new File(path);
                                    if(file.renameTo(newFile)) {
                                        getFileData(mCurrentPath);
                                        Toast.makeText(FileBrowserActivity.this, "重命名成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(FileBrowserActivity.this, "重命名失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .create()
                        .show();
            } else {
                Toast.makeText(FileBrowserActivity.this, "文件名不存在", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
