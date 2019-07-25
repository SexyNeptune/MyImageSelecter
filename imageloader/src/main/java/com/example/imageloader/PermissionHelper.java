package com.example.imageloader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {
    private Activity activity;
    private PermissionInterface permissionInterface;
    private int callBackCode;

    public interface PermissionInterface {
        void requestPermissionsSuccess(int callBackCode);
        void requestPermissionsFail(int callBackCode);
    }

    public PermissionHelper(Activity activity, PermissionInterface permissionInterface){
        this.activity = activity;
        this.permissionInterface = permissionInterface;
    }

    public void requestPermission(String permission, int callBackCode){
        this.callBackCode = callBackCode;
        if (hasPermission(activity,permission)){
            permissionInterface.requestPermissionsSuccess(callBackCode);
        }else{
            ActivityCompat.requestPermissions(activity,new String[]{permission},callBackCode);
        }
    }

    /**
     * 判断是否有某个权限
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0判断，6.0以下跳过。在清单文件注册即可，不用动态请求，这里直接视为有权限
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     *  在Activity中的onRequestPermissionsResult中调用,用来接收结果判断
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==callBackCode){
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionInterface.requestPermissionsFail(callBackCode);
                    break;
                }
            }
            permissionInterface.requestPermissionsSuccess(callBackCode);
        }
    }

}