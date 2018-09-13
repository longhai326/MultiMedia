package com.lh.mediatest.util;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    private static final String TAG = "LH/PermissionUtils";
    public static final int CODE_RECORD_AUDIO = 0;
    public static final int CODE_READ_PHONE_STATE = 1;
    public static final int CODE_ACCESS_FINE_LOCATION = 2;
    public static final int CODE_ACCESS_COARSE_LOCATION = 3;
    public static final int CODE_READ_EXTERNAL_STORAGE = 4;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 5;
    public static final int CODE_CAMERA = 6;
    // public static final int CODE_GET_ACCOUNTS = ;
    // public static final int CODE_CALL_PHONE = ;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    // public static final String PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    // public static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;


    public static final String[] PERMISSIONS_HINT = new String[]{
            "录音权限",
            "读取手机状态权限",
            "GPS定位权限",
            "网络定位权限",
            "外置存储读取权限",
            "外置存储写入权限",
            "照相机权限"
            // "未获得获取用户账号权限",
            // "拨打电话权限",

    };

    private static final String[] requestPermissions = {
            PERMISSION_RECORD_AUDIO,
            PERMISSION_READ_PHONE_STATE,
            PERMISSION_ACCESS_FINE_LOCATION,
            PERMISSION_ACCESS_COARSE_LOCATION,
            PERMISSION_READ_EXTERNAL_STORAGE,
            PERMISSION_WRITE_EXTERNAL_STORAGE,
            PERMISSION_CAMERA
            // PERMISSION_GET_ACCOUNTS,
            // PERMISSION_CALL_PHONE,
    };

    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * 一次申请多个权限
     * 需要在Activity重写权限监听的方法
     */
    public static void requestMultiPermissions(final Activity activity, PermissionGrant grant) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null!");
        }
        final List<String> permissionsList = new ArrayList<>();
        final List<String> shouldRationalePermissionsList = new ArrayList<>();
        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];
            int checkSelfPermission;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Log.e(TAG, "CheckSelfPermission exception : " + e.getMessage());
                Toast.makeText(activity, "请到设置界面打开权限 : " + PERMISSIONS_HINT[i], Toast.LENGTH_LONG).show();
                break;
            }
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "没有权限：" + requestPermission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()会返回true
                    // 可以向用户解释为什么需要这个权限
                    shouldRationalePermissionsList.add(requestPermission);
                } else {
                    permissionsList.add(requestPermission);
                }
            }
        }
        if (permissionsList.isEmpty() && shouldRationalePermissionsList.isEmpty()) {
            Log.d(TAG, "Permissions all granted");
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);
            return;
        }
        Log.d(TAG, "requestMultiPermissions permissions:" + permissionsList.size() + ",rationalePermissions:" + shouldRationalePermissionsList.size());
        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
                    CODE_MULTI_PERMISSION);
            Log.d(TAG, "requestPermissions");
        }
        if (shouldRationalePermissionsList.size() > 0) {
            showMessageOKCancel(activity, "应用需要打开以下权限才能使用",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
                                    CODE_MULTI_PERMISSION);
                            Log.d(TAG, "requestRationalePermissions");
                        }
                    });
        }
    }

    /**
     * 请求一个权限
     */
    public static void requestPermission(final Activity activity, final int requestCode, PermissionGrant grant) {
        if (activity == null) {
            throw new NullPointerException("activity cannot be null!");
        }
        Log.d(TAG, "requestPermission requestCode:" + requestCode);
        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Log.e(TAG, "requestPermission illegal requestCode:" + requestCode);
            return;
        }
        final String requestPermission = requestPermissions[requestCode];
        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 建议try{}catch(){}单独处理，提示用户开启权限。
//        if (Build.VERSION.SDK_INT < 23) {
//            return;
//        }
        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "请到设置界面打开权限 : " + PERMISSIONS_HINT[requestCode], Toast.LENGTH_SHORT).show();
            Log.e(TAG, "RuntimeException:" + e.getMessage());
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "没有权限：" + requestPermission);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                Log.i(TAG, "requestRationalePermission");
                showMessageOKCancel(activity, "应用需要打开权限: " + PERMISSIONS_HINT[requestCode], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{requestPermission},
                                requestCode);
                    }
                });
            } else {
                Log.d(TAG, "requestPermission");
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }

        } else {
            grant.onPermissionGranted(requestCode);
        }
    }

    private static void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private static void openSettingActivity(final Activity activity, String message) {
        showMessageOKCancel(activity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Log.d(TAG, "getPackageName(): " + activity.getPackageName());
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }

}
