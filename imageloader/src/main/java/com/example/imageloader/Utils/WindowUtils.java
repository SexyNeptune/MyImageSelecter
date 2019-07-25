package com.example.imageloader.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

public class WindowUtils {

    /**
     * 设置状态栏字体颜色
     * @param activity
     * @param dark true：灰色字体  false ： 白色字体
     */
    public static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (dark) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    /**
     * 利用反射获取状态栏高度
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId =context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}
