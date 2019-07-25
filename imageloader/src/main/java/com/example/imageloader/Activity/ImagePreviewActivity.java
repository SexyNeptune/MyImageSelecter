package com.example.imageloader.Activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.imageloader.R;
import com.example.imageloader.Utils.WindowUtils;
import com.example.imageloader.ui.ZoomImageView;

import java.util.ArrayList;

public class ImagePreviewActivity extends AppCompatActivity {

    private FrameLayout mBtnBack;
    private ViewPager viewPager;
    private RelativeLayout rl_top_bar;

    private ArrayList<String> paths;
    private static final String TAG = ImagePreviewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        findView();
        setStatusBar();
        initData();
        setListener();
    }

    private void setStatusBar() {
        WindowUtils.setAndroidNativeLightStatusBar(this, true);
    }

    private void findView() {
        mBtnBack = findViewById(R.id.btn_back);
        viewPager = findViewById(R.id.image_preview_viewpager);
        rl_top_bar = findViewById(R.id.rl_top_bar);
    }

    private void initData() {
        paths = getIntent().getStringArrayListExtra("paths");
        int position = getIntent().getIntExtra("position", 0);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return paths.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ZoomImageView imageView = new ZoomImageView(ImagePreviewActivity.this);
                imageView.setImageBitmap(BitmapFactory.decodeFile(paths.get(position)));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rl_top_bar.getVisibility() == View.GONE) {
                            rl_top_bar.setVisibility(View.VISIBLE);
                            //显示状态栏字体且设置为灰色
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        } else if (rl_top_bar.getVisibility() == View.VISIBLE) {
                            rl_top_bar.setVisibility(View.GONE);
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                        Log.e(TAG, "ZoomImageView : onClick()");
                    }
                });
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });
        viewPager.setCurrentItem(position);
    }

    private void setListener() {
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rl_top_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"rl_top_bar : onClick()");
            }
        });

        viewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rl_top_bar.getVisibility() == View.VISIBLE) {
                    rl_top_bar.setVisibility(View.GONE);
                } else if (rl_top_bar.getVisibility() == View.GONE) {
                    rl_top_bar.setVisibility(View.VISIBLE);
                }
                Log.e(TAG,"viewPager :  onClick");
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                Log.e(TAG, "onPageSelected");
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.e(TAG, "onPageScrollStateChanged");
            }
        });
    }
}
