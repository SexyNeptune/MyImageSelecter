package com.example.imageloader.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imageloader.PermissionHelper;
import com.example.imageloader.R;
import com.example.imageloader.Utils.WindowUtils;
import com.example.imageloader.adapter.ImageRecyclerAdapter;
import com.example.imageloader.bean.Folder;
import com.example.imageloader.model.ImageModel;
import com.example.imageloader.ui.MyFolderListPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageListActivity extends AppCompatActivity implements ImageModel.ImageContentCallBack {

    //工作线程,子线程
    private Handler workHandler, mainHandler;
    private HandlerThread mHandlerThreaad;
    private PermissionHelper permissionHelper;

    private FrameLayout btnBack;
    private RecyclerView mRvImageList;
    private TextView mTvDirName;
    private TextView mTvPreview;
    private RelativeLayout mRlBottomBar;

    //    private ArrayList<String> imgPaths = new ArrayList<>();
    private ImageRecyclerAdapter adapter;
    private File mCurrentDir;
    private List<Folder> mFolders = new ArrayList<>();

    private int columnCount;
    private String loadModel;


    public static void actionStart(Context context, int columnCount) {
        Intent intent = new Intent(context, ImageListActivity.class);
        intent.putExtra("columnCount", columnCount);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);
        requestPermissions();
        setStatusBar();
        findView();
//        initData();
        setListener();
    }

    private void requestPermissions() {
        //权限处理
        permissionHelper = new PermissionHelper(this, new PermissionHelper.PermissionInterface() {
            @Override
            public void requestPermissionsSuccess(int callBackCode) {
                initData();
            }

            @Override
            public void requestPermissionsFail(int callBackCode) {
                Toast.makeText(ImageListActivity.this, "没有权限读取本地数据", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        permissionHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,1);
    }

    private void setStatusBar() {
        WindowUtils.setAndroidNativeLightStatusBar(this, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
        }
    }

    public void findView() {
        btnBack = findViewById(R.id.btn_back);
        mRvImageList = findViewById(R.id.rv_image_list);
        mRlBottomBar = findViewById(R.id.rl_bottom_bar);
        mTvDirName = findViewById(R.id.tv_dir_name);
        mTvPreview = findViewById(R.id.tv_preview);
    }

    private void initData() {
        columnCount = getIntent().getIntExtra("columnCount", 3);
        //这里的所有数据加载都放在了线程中
        //故所有的UI操作也要在mainHandler中执行
//        initMainThreadHandler();
//        initWorkThread();
//        //加载数据的起点
//        workHandler.sendEmptyMessage(0);
        ImageModel.loadImageFromSDCard(this,this);
    }

//    private void initMainThreadHandler() {
//        mainHandler = new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                GridLayoutManager layoutManager = new GridLayoutManager(ImageListActivity.this, columnCount);
//                mRvImageList.setLayoutManager(layoutManager);
//                adapter = new ImageRecyclerAdapter(mCurrentDir);
//                mRvImageList.setAdapter(adapter);
//                mTvDirName.setText(mFolders.get(0).getName());
//                return false;
//            }
//        });
//    }
//
//    /**
//     * 创建工作子线程负责读取图片
//     */
//    private void initWorkThread() {
//        mHandlerThreaad = new HandlerThread("handlerThread");
//        mHandlerThreaad.start();
//        //子线程工作
//        workHandler = new Handler(mHandlerThreaad.getLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                //由于扫描图片是耗时的操作，所以要在子线程处理。
//                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                ContentResolver mContentResolver = getContentResolver();
//                Cursor mCursor = mContentResolver.query(mImageUri, new String[]{MediaStore.Images.Media.DATA},
//                        null, null, MediaStore.Images.Media.DATE_ADDED); //按照现在的写法,最后的排序参数只影响相册目录的首张图片的显示
//
//                //一个文件夹对应一个父路径
//                Set<String> mParentDirSet = new HashSet<>();
//
//                //开始扫描
//                while (mCursor.moveToNext()) {
//                    // 获取图片的路径
//                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
////                    imgPaths.add(path);
//                    //根据图片路径得到父路径文件
//                    File parentFile = new File(path).getParentFile();
//                    if (parentFile == null) {
//                        continue;
//                    }
//                    if (mCurrentDir == null) {
//                        //当前文件夹为读取到第一张图片的相册
//                        mCurrentDir = parentFile;
//                    }
//                    String parentDir = parentFile.getAbsolutePath();
//                    //防止重复加载同一文件夹下的图片，提高加载效率
//                    if (mParentDirSet.contains(parentDir)) {
//                        continue;
//                    } else {
//                        mParentDirSet.add(parentDir);
//                    }
//
//                    //设置文件夹实体类
//                    Folder folder = new Folder();
//                    folder.setDir(parentDir);
//                    folder.setFirstImagePath(path);
//                    int imageCount = parentFile.list(new FilenameFilter() {
//                        @Override
//                        public boolean accept(File dir, String name) {
//                            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
//                                return true;
//                            }
//                            return false;
//                        }
//                    }).length;
//                    folder.setCount(imageCount);
//                    //Folder创建完成，加入列表
//                    mFolders.add(folder);
//                }
//                //扫描结束
//                mCursor.close();
//                Log.e(TAG, mFolders.toString());
//                //通知主线程显示图片
//                mainHandler.sendEmptyMessage(0);
//            }
//        };
//    }


    public void setListener() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvDirName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyFolderListPopupWindow popupWindow = new MyFolderListPopupWindow(ImageListActivity.this, mFolders);
                popupWindow.setAnimationStyle(R.style.anim_popup_translate);
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        lightUp();
                    }
                });
                popupWindow.showAsDropDown(mRlBottomBar, 0, 0);
                lightDown();
                popupWindow.setOnItemClickListener(new MyFolderListPopupWindow.OnItemSelectListener() {
                    @Override
                    public void onSelect(Folder folder) {
                        mCurrentDir = new File(folder.getDir());
                        adapter = new ImageRecyclerAdapter(mCurrentDir);
                        mRvImageList.setAdapter(adapter);
                        mTvDirName.setText(folder.getName());
                        popupWindow.dismiss();
                    }
                });
            }
        });
    }

    /**
     * 内容区变亮
     */
    private void lightUp() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * 内容区变暗
     */
    private void lightDown() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = .3f;
        getWindow().setAttributes(lp);
    }

    @Override
    public void Call(List<Folder> folders,File currentFile) {
        //全局变量赋值
        mCurrentDir = currentFile;
        mFolders = folders;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GridLayoutManager layoutManager = new GridLayoutManager(ImageListActivity.this, columnCount);
                mRvImageList.setLayoutManager(layoutManager);
                adapter = new ImageRecyclerAdapter(mCurrentDir);
                mRvImageList.setAdapter(adapter);
                mTvDirName.setText(mFolders.get(0).getName());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.requestPermissionsResult(requestCode,permissions,grantResults);
    }
}
