package com.example.imageselecter.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.imageloader.Utils.ImageSelector;
import com.example.imageloader.Utils.WindowUtils;
import com.example.imageselecter.R;



public class MainActivity extends AppCompatActivity {

    private Button mBtnSelect;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //实例化控件
        mBtnSelect = findViewById(R.id.main_select_image);
        mImage = findViewById(R.id.main_img);
        mBtnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageSelector.build().setColumnCount(4).start(MainActivity.this);
            }
        });
        //设置状态栏的主题颜色
        WindowUtils.setAndroidNativeLightStatusBar(this,true);
    }

}
