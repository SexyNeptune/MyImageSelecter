package com.example.imageloader.Utils;

import android.content.Context;

import com.example.imageloader.Activity.ImageListActivity;


public class ImageSelector {

    public static ImageSelectorBuilder build(){
        return new ImageSelectorBuilder();
    }

    public static class ImageSelectorBuilder{
        /**
         * 图片列表的列数  -- default：3
         */
        private int columnCount = 3;


        public ImageSelectorBuilder setColumnCount(int columnCount) {
            this.columnCount = columnCount;
            return this;
        }

        public void start(Context context){
            ImageListActivity.actionStart(context, columnCount);
        }

    }

}
