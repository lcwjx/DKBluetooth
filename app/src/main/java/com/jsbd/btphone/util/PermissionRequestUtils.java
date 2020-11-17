package com.jsbd.btphone.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.jsbd.bluetooth.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙模块让外围apk自己管理权限
 */
public class PermissionRequestUtils {
    private static final String TAG = "PermissionRequestUtils";

    public static final int CODE_CONTACTS = 0;
    public static final int CODE_PHONE = 1;
    public static final int CODE_CALENDER = 2;
    public static final int CODE_CAMERA = 3;
    public static final int CODE_SENSORS = 4;
    public static final int CODE_LOCATION = 5;
    public static final int CODE_STORAGE = 6;
    public static final int CODE_MICROPHONE = 7;
    public static final int CODE_SMS = 8;
    public static final int CODE_MULTI_PERMISSION = 100;

    /**
     * group:android.permission-group.CONTACTS
     * permission:android.permission.WRITE_CONTACTS
     * permission:android.permission.GET_ACCOUNTS
     * permission:android.permission.READ_CONTACTS
     */
    public static final String PERMISSION_CONTACTS = Manifest.permission.WRITE_CONTACTS;

    /**
     * group:android.permission-group.PHONE
     * permission:android.permission.READ_CALL_LOG
     * permission:android.permission.READ_PHONE_STATE
     * permission:android.permission.CALL_PHONE
     * permission:android.permission.WRITE_CALL_LOG
     * permission:android.permission.USE_SIP
     * permission:android.permission.PROCESS_OUTGOING_CALLS
     * permission:com.android.voicemail.permission.ADD_VOICEMAIL
     */
    public static final String PERMISSION_PHONE = Manifest.permission.READ_CALL_LOG;

    /**
     * group:android.permission-group.CALENDAR
     * permission:android.permission.READ_CALENDAR
     * permission:android.permission.WRITE_CALENDAR
     */
    public static final String PERMISSION_CALENDAR = Manifest.permission.READ_CALENDAR;

    /**
     * group:android.permission-group.CAMERA
     * permission:android.permission.CAMERA
     */
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    /**
     * group:android.permission-group.SENSORS
     * permission:android.permission.BODY_SENSORS
     */
    public static final String PERMISSION_SENSORS = Manifest.permission.BODY_SENSORS;

    /**
     * group:android.permission-group.LOCATION
     * permission:android.permission.ACCESS_FINE_LOCATION
     * permission:android.permission.ACCESS_COARSE_LOCATION
     */
    public static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    /**
     * group:android.permission-group.STORAGE
     * permission:android.permission.READ_EXTERNAL_STORAGE
     * permission:android.permission.WRITE_EXTERNAL_STORAGE
     */
    public static final String PERMISSION_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    /**
     * group:android.permission-group.MICROPHONE
     * permission:android.permission.RECORD_AUDIO
     */
    public static final String PERMISSION_MICROPHONE = Manifest.permission.RECORD_AUDIO;

    /**
     * group:android.permission-group.SMS
     * permission:android.permission.READ_SMS
     * permission:android.permission.RECEIVE_WAP_PUSH
     * permission:android.permission.RECEIVE_MMS
     * permission:android.permission.RECEIVE_SMS
     * permission:android.permission.SEND_SMS
     * permission:android.permission.READ_CELL_BROADCASTS
     */
    public static final String PERMISSION_SMS = Manifest.permission.READ_SMS;

    private static final String[] requestPermissions = {
            PERMISSION_CONTACTS,
            PERMISSION_PHONE,
            PERMISSION_CALENDAR,
            PERMISSION_CAMERA,
            PERMISSION_SENSORS,
            PERMISSION_LOCATION,
            PERMISSION_STORAGE,
            PERMISSION_MICROPHONE,
            PERMISSION_SMS,
    };

    //callback for request permission result
    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * request permission.
     * Single one
     */
    public static void requestPermission(Activity activity, int requestCode,
                                         PermissionGrant permissionGrant) {
        if (activity == null) {
            return;
        }

        if (!checkRequestCode(requestCode)) {
            return;
        }

        String permission = requestPermissions[requestCode];

        int checkSelfPermission;

        checkSelfPermission = ActivityCompat.checkSelfPermission(activity, permission);

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        } else {
//            Toast.makeText(activity, "opened:" + requestPermissions[requestCode], Toast.LENGTH_SHORT).show();
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    public static void requestMultiPermissions(Activity activity, int[] requestCode, PermissionGrant permissionGrant) {
        List<String> permissionList = new ArrayList<>();

        if (activity == null) {
            return;
        }

        for (int i : requestCode) {
            if (!checkRequestCode(i)) {
                return;
            }
        }

        for (int rc : requestCode) {
            String permission = requestPermissions[rc];

            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionList.toArray(new String[permissionList.size()]), CODE_MULTI_PERMISSION);
        }
    }

    private static boolean checkRequestCode(int requestCode) {
        if (requestCode < 0 || requestCode > requestPermissions.length - 1) {
            LogUtils.e(TAG, "requestPermission illegal requestCode : " + requestCode);
            return false;
        }
        return true;
    }

    /**
     *
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }

        LogUtils.d(TAG, "requestPermissionsResult requestCode:" + requestCode);

        if (requestCode == CODE_MULTI_PERMISSION) {

//            requestMultiResult(activity, permissions, grantResults, permissionGrant);
            return;
        }

        if (!checkRequestCode(requestCode)) {
            return;
        }

//        L.si("onRequestPermissionsResult requestCode:" + requestCode + ",permissions:" + permissions.toString()
//                + ",grantResults:" + grantResults.toString() + ",length:" + grantResults.length);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LogUtils.i(TAG, "onRequestPermissionsResult PERMISSION_GRANTED");
            //callback, can do something when needed
            permissionGrant.onPermissionGranted(requestCode);

        } else {
            LogUtils.i(TAG, "onRequestPermissionsResult PERMISSION NOT GRANTED");
//            Toast.makeText(activity, "request Permission Fail " + requestCode, Toast.LENGTH_LONG).show();

        }

    }

}