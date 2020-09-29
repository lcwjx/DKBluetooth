package com.jsbd.btphone.module.base;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.jsbd.btphone.R;

/**
 * Created by wills on 16/3/2.
 */
public class BaseDialog extends Dialog {

    private BaseActivity baseActivity;

    public BaseDialog(Context context) {
        this(context, R.style.SimpleDialog);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);

        if (context instanceof BaseActivity) {
            ((BaseActivity) context).catchDialog(this);
            baseActivity = ((BaseActivity) context);
        }
    }

    public BaseActivity getActivity() {
        return baseActivity;
    }


    public <T extends View> T bindView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void show() {
        if (baseActivity != null && baseActivity.isActivityInited()
                && !baseActivity.isOnDestroy()) {
            super.show();
        } else {
            super.show();
        }
    }


    @Override
    public void dismiss() {
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).removeDialog(this);
        }
        super.dismiss();
    }

    public void show(int animation) {
        getWindow().setWindowAnimations(animation); //设置窗口弹出动画
        show();
    }

    /**
     * 设置高度,重心
     */
    public void setHeight(int height, int gravity) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.height = height;
        getWindow().setAttributes(lp);
        getWindow().setGravity(gravity);
    }

//    /**
//     * 设置宽度全屏，高度适应
//     */
//    public void setFullWidthScreen(int gravity) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = MeasureUtil.heightPixels;
//        getWindow().setAttributes(lp);
//        getWindow().setGravity(gravity);
//    }
//
//    public void setFullScreen() {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = MeasureUtil.heightPixels;
//        lp.height = MeasureUtil.widthPixels;
//        getWindow().setAttributes(lp);
//    }
}
