package com.jsbd.btphone.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * Created by wills on 15/10/15.
 */
public class SoftInputUtil {
    private final static String TAG = "SoftInputUtil";
    private static boolean sLastVisiable = false;

    public static void setOnFocus(EditText editText, boolean isOnFocus) {
        if (editText == null) return;
        try {
            Class<EditText> cls = EditText.class;
            Method method;
            if (Build.VERSION.SDK_INT >= 16) {
                method = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
            } else {
                method = cls.getMethod("setSoftInputShownOnFocus",
                        boolean.class);
            }
            if (method != null) {
                method.setAccessible(true);
                method.invoke(editText, isOnFocus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideSoftInput(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) view
                .getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        view.clearFocus();
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(
                    view.getApplicationWindowToken(), 0);

            imm.hideSoftInputFromWindow(
                    view.getWindowToken(), 0);
        }
    }

    public static void showSoftInput(EditText editText) {
        if (editText == null) return;
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        do {
            editText.requestFocus();
        } while (!editText.isFocused());

        if (imm.isActive()) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 监听软键盘状态
     *
     * @param activity
     * @param listener
     */
    public static void addOnSoftKeyBoardVisibleListener(Activity activity, final OnSoftKeyBoardVisibleListener listener) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHight = rect.bottom - rect.top;
                int hight = decorView.getHeight();
                boolean visible = (double) displayHight / hight < 0.8;

                if (visible != sLastVisiable) {
                    if (listener != null) {
                        listener.onSoftKeyBoardVisible(visible);
                    }
                }
                sLastVisiable = visible;
            }
        });
    }

    public interface OnSoftKeyBoardVisibleListener {
        void onSoftKeyBoardVisible(boolean visible);
    }
}

