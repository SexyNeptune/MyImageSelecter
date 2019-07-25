package com.example.imageloader.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.imageloader.Activity.ImagePreviewActivity;
import com.example.imageloader.R;
import com.example.imageloader.Utils.ImageLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {

    private ArrayList<String> imagePaths = new ArrayList<>();
    private Context context;

    public ImageRecyclerAdapter(File file){
        //获得文件夹下所有图片路径
        String dir = file.getAbsolutePath();
        String childNames[]  = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        });
        for (String name : childNames) {
            imagePaths.add(dir + "/" +  name);
        }
//        sortImages();
    }

    /**
     * 按文件的修改时间降序
     */
    private void sortImages() {
        List<File> fileList = new ArrayList();
        for (String path : imagePaths) {
            fileList.add(new File(path));
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.lastModified() < o2.lastModified()) {
                    return 1;
                }else{
                    return -1;
                }
            }
        });
        imagePaths.clear();
        for (File file : fileList) {
            imagePaths.add(file.getAbsolutePath());
        }
    }


    @NonNull
    @Override
    public ImageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_image_list,viewGroup,false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Intent intent = new Intent(context, ImagePreviewActivity.class);
                intent.putExtra("position", position);
                intent.putStringArrayListExtra("paths", imagePaths);
                context.startActivity(intent);
            }
        });
        return viewHolder;
    }


    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull ImageRecyclerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.imageView.setImageDrawable(context.getDrawable(R.drawable.image));
        String path = imagePaths.get(i);
        ImageLoader.getInstance().loadImage(path,viewHolder.imageView);
        Log.e("RecyclerAdapter","第" + i + "个item");
//        Glide.with(context).load(path).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return imagePaths ==null ? 0 : imagePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.rv_item_image);
        }
    }
}
