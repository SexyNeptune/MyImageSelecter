package com.example.imageloader.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import com.example.imageloader.bean.Folder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageModel {

    public interface ImageContentCallBack{
        void Call(List<Folder> folders, File currentFile);
    }

    private static  final String TAG = "ImageModel";

    public static void loadImageFromSDCard(final Context context, final ImageContentCallBack imageContentCallBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //由于扫描图片是耗时的操作，所以要在子线程处理。
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = context.getContentResolver();
                Cursor mCursor = mContentResolver.query(mImageUri, new String[]{MediaStore.Images.Media.DATA},
                        null, null, MediaStore.Images.Media.DATE_ADDED); //按照现在的写法,最后的排序参数只影响相册目录的首张图片的显示

                //一个文件夹对应一个父路径
                Set<String> mParentDirSet = new HashSet<>();
                File mCurrentDir = null;
                List<Folder> mFolders = new ArrayList<>();

                //开始扫描
                while (mCursor.moveToNext()) {
                    // 获取图片的路径
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                    imgPaths.add(path);
                    //根据图片路径得到父路径文件
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    if (mCurrentDir == null) {
                        //当前文件夹为读取到第一张图片的相册
                        mCurrentDir = parentFile;
                    }
                    String parentDir = parentFile.getAbsolutePath();
                    //防止重复加载同一文件夹下的图片，提高加载效率
                    if (mParentDirSet.contains(parentDir)) {
                        continue;
                    }else{
                        mParentDirSet.add(parentDir);
                    }

                    //设置文件夹实体类
                    Folder folder = new Folder();
                    folder.setDir(parentDir);
                    folder.setFirstImagePath(path);
                    int imageCount = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")){
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    folder.setCount(imageCount);
                    //Folder创建完成，加入列表
                    mFolders.add(folder);
                }
                //扫描结束
                mCursor.close();
                Log.e(TAG, mFolders.toString());
                //通知主线程显示图片
//        mainHandler.sendEmptyMessage(0);
                imageContentCallBack.Call(mFolders, mCurrentDir);
            }
        }).start();

    }

}
