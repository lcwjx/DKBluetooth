package com.jsbd.btphone.util;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;


import com.jsbd.support.bluetooth.utils.TextUtil;

import java.util.Collection;
import java.util.List;

/**
 * Created by qy128 on 2018/10/31.
 */

public class DBBtUtil {
    private static final String TAG = DBBtUtil.class.getSimpleName();

    /**
     * 判断集合是否为空
     *
     * @param collection
     * @return
     */
    public static boolean isCollectionEmpty(Collection collection) {
        return (collection == null || collection.size() == 0);
    }


    /**
     * 根据高亮字符串支持变色的电话号码，只是空格分隔的电话号码
     *
     * @param number         电话号码
     * @param highlight      需要高亮字符串
     * @param highlightColor 高亮颜色值
     * @return 高亮后的字符串
     */
    public static SpannableString parserHighLightText(String number, String highlight, @ColorInt int highlightColor) {
        if (TextUtil.isEmpty(number)) return new SpannableString("");

        if (TextUtil.isEmpty(highlight)) {
            return new SpannableString(number);
        }

        char[] numberChars = number.toCharArray();
        char[] highlightChars = highlight.toCharArray();

        int start = -1;
        int end = 0;
        int j = 0;
        int i = 0;
        for (; i < highlightChars.length; i++) {
            boolean bFound = false;
            if (start == -1) {
                for (; j < numberChars.length; j++) {
                    if (numberChars[j] == highlightChars[i]) {
                        bFound = true;
                        start = end = j;
                        break;
                    }
                }
            } else {
                if (numberChars[end] == ' ') {
                    end++;
                }

                if (highlightChars[i] == numberChars[end]) {
                    bFound = true;
                } else {
                    j = end;
                    i = -1;

                    start = -1;
                    end = 0;
                    continue;
                }
            }

            if (bFound) {
                end++;
            } else {
                start = -1;
                end = 0;
                break;
            }
        }

        SpannableString spannableString = new SpannableString(number);
        if (start >= 0) {
            spannableString.setSpan(new ForegroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    /*设置显示长度*/
    public static String handleText(String str, int maxLen) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }

        int count = 0;
        int endIndex = 0;
        for (int i = 0; i < str.length(); i++) {
            char item = str.charAt(i);
            if (item < 128) {
                count = count + 1;
            } else {
                count = count + 2;
            }

            if (maxLen == count || (item >= 128 && maxLen + 1 == count)) {
                endIndex = i;
            }
        }

        if (count <= maxLen) {
            return str;
        } else {
            return str.substring(0, endIndex) + "...";
        }
    }

    /*电话号码格式化显示*/
    public static String numberFormat(String number) {
        if (number == null || number.length() == 0) return number;

        String newNumber = number.replace(" ","");
        if (newNumber.length() > 11) {
            newNumber = newNumber.substring(0, 3) + " " + newNumber.substring(3, 7) + " " + newNumber.substring(7, 11) + " "
                    + newNumber.substring(11, newNumber.length());
        } else if (newNumber.length() <= 11 && newNumber.length() > 7) {
            newNumber = newNumber.substring(0, 3) + " " + newNumber.substring(3, 7) + " " + newNumber.substring(7, newNumber.length());
        } else if (newNumber.length() <= 7 && newNumber.length() > 3) {
            newNumber = newNumber.substring(0, 3) + " " + newNumber.substring(3, newNumber.length());
        }

        return newNumber;
    }

    public static String getForegroundActivityClassName(Context context) {
        if (context == null) {
            return null;
        }

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfos = mActivityManager.getRunningTasks(1);
        if (taskInfos != null && taskInfos.size() > 0) {
            ActivityManager.RunningTaskInfo taskInfo = taskInfos.get(0);
            return taskInfo.topActivity.getClassName();
        }
        return null;
    }

    public static void sendKeyEvent2(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void startLauncher(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.jsbd.launcher");
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
