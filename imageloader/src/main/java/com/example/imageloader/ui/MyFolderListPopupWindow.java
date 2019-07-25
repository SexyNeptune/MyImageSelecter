package com.example.imageloader.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.imageloader.R;
import com.example.imageloader.Utils.ImageLoader;
import com.example.imageloader.bean.Folder;

import java.util.List;

public class MyFolderListPopupWindow extends PopupWindow {

    private int mWidth;
    private int mHeight;
    private OnItemSelectListener mListener;

    public interface OnItemSelectListener {
        void onSelect(Folder folder);
    }

    public void setOnItemClickListener(OnItemSelectListener mListener){
        this.mListener = mListener;
    }

    public MyFolderListPopupWindow(final Context context, final List<Folder> folders) {
        calculateSize(context);

        View contentView = LayoutInflater.from(context).inflate(R.layout.item_popup_folder, null);

        setContentView(contentView);
        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);
//        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }else{
                    return false;
                }
            }
        });

        ListView mListView = contentView.findViewById(R.id.item_popup_folder_list);
        mListView.setAdapter(new FolderListAdapter(context,folders));
    }


    private void calculateSize(Context context) {

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mWidth = displayMetrics.widthPixels;
        mHeight = (int) (displayMetrics.heightPixels *0.75);
    }

    private class FolderListAdapter extends ArrayAdapter<Folder> {

        private LayoutInflater mInflater;

        FolderListAdapter(Context context, List<Folder> objects) {
            super(context, 0, objects);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView,  ViewGroup parent) {
            ViewHolder holder ;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_lv_folder, parent,false);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.item_lv_image);
                holder.mTvName = convertView.findViewById(R.id.item_lv_folder_name);
                holder.mTvCount = convertView.findViewById(R.id.item_lv_folder_counts);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            final Folder folder = getItem(position);
            //防止复用
            holder.imageView.setImageResource(R.drawable.image);
            holder.imageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onSelect(folder);
                    }
                }
            });
            ImageLoader.getInstance().loadImage(folder.getFirstImagePath(), holder.imageView);
            holder.mTvName.setText(folder.getName());
            holder.mTvCount.setText(folder.getCount() + "");

            return convertView;
        }

        private class ViewHolder{
            ImageView imageView;
            TextView mTvName;
            TextView mTvCount;
        }
    }

}
