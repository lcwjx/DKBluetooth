package com.jsbd.btphone.module.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.jsbd.btphone.R;
import com.jsbd.btphone.module.base.BaseDialog;
import com.jsbd.support.bluetooth.utils.LogUtils;

/**
 * Created by qy128 on 2018/10/27.
 */

public class ProgressDialog extends BaseDialog {
    private static final String TAG = ProgressDialog.class.getSimpleName();

    private ImageView loadingImageView;
    private TextView loadingTextView;
    private Animation operatingAnim;

    public ProgressDialog(Context context) {
        this(context, R.style.CustomProgressDialog);
    }

    public ProgressDialog(final Context context, int theme) {
        super(context, theme);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(R.layout.contacts_sync_loading_layout);
        getWindow().getAttributes().gravity = Gravity.CENTER;

        loadingImageView = (ImageView) findViewById(R.id.loadingiv);
        loadingTextView = (TextView) findViewById(R.id.loadingtv);
        operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());

        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0) {
                    ((Activity) context).moveTaskToBack(true);
                }
                return false;
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogUtils.d(TAG, "ProgressDialog >> onAttachedToWindow ");
        startAnimation();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.d(TAG, "ProgressDialog >> onDetachedFromWindow ");
        stopAnimation();
    }

    private void startAnimation() {
        if (loadingImageView != null) {
            loadingImageView.setBackgroundResource(R.mipmap.ic_loading);
            loadingImageView.startAnimation(operatingAnim);
        }
    }

    private void stopAnimation() {
        if (loadingImageView != null) {
            loadingImageView.setBackgroundResource(R.mipmap.ic_loading);
            loadingImageView.clearAnimation();
        }
    }

    public void setLoading(int count) {
        if (loadingTextView != null) {
            loadingTextView.setText(getContext().getResources().getString(R.string.bt_contacts_sync_loading_status) + "(" + count + ")");
        }
    }

    public void setComplete(int count) {
        stopAnimation();
        if (loadingImageView != null) {
            loadingImageView.setBackgroundResource(R.mipmap.ic_complete);
        }

        if (loadingTextView != null) {
            loadingTextView.setText(getContext().getResources().getString(R.string.bt_contacts_sync_loaded_status) + "(" + count + ")");
        }
    }

    @Override
    public void dismiss() {
        if (isShowing()) {
            LogUtils.d(TAG, "ProgressDialog >> dismiss ");
            super.dismiss();
        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            LogUtils.d(TAG, "ProgressDialog >> show ");
            super.show();
        }
    }

    /**
     * [Summary]
     * setMessage 提示内容
     *
     * @param strMessage
     * @return
     */
    public ProgressDialog setMessage(int strMessage) {
        if (loadingTextView != null) {
            loadingTextView.setText(strMessage);
        }

        return this;
    }
}