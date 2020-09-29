package com.jsbd.btphone.module.base;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jsbd.btphone.util.DBBtUtil;

import java.util.ArrayList;
import java.util.List;


public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private List<BroadcastReceiver> mListReceiver;
    private List<Dialog> mListDialog;
    private List<PopupWindow> mListPopupWindow;

    private boolean mIsDestroyed = false;
    private boolean mIsResumed = false;

    private BaseFragment mCurrFragment;
    private List<ActivityResultCallBack> mListResultCallBack = new ArrayList<>();
    private boolean mIsActivityInited = false;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setCurrFragment(BaseFragment currFragment) {
        mCurrFragment = currFragment;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mIsActivityInited = true;
        }
    }

    public boolean isActivityInited() {
        return mIsActivityInited;
    }

    protected void registerReceiver(final BroadcastReceiver receiver, String... filters) {
        IntentFilter filter = new IntentFilter();
        for (String f : filters) {
            filter.addAction(f);
        }

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isOnDestroy() && receiver != null) {
                    receiver.onReceive(context, intent);
                }
            }
        };

        registerReceiver(broadcastReceiver, filter);

        if (mListReceiver == null)
            mListReceiver = new ArrayList<>();

        mListReceiver.add(broadcastReceiver);
    }

    protected void registerFinishReceiver(String... filters) {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        }, filters);
    }

    protected void registerExitAppReceiver(String... filters) {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        }, filters);
    }

    public void catchDialog(Dialog dialog) {
        if (mListDialog == null)
            mListDialog = new ArrayList<>();
        mListDialog.add(dialog);
    }

    public void removeDialog(Dialog dialog) {
        if (mListDialog != null) {
            mListDialog.remove(dialog);
        }
    }

    public void catchPopupWindow(PopupWindow popupWindow) {
        if (mListPopupWindow == null)
            mListPopupWindow = new ArrayList<>();

        if (!mListPopupWindow.contains(popupWindow))
            mListPopupWindow.add(popupWindow);
    }

    public void removePopupWindow(PopupWindow popupWindow) {
        if (mListPopupWindow != null) {
            mListPopupWindow.remove(popupWindow);
        }
    }

    public BaseActivity getActivity() {
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        //L.i(TAG, "BaseActivity >> onResume");
        mIsResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        //L.i(TAG, "BaseActivity >> onPause");
        mIsResumed = false;
    }

    public boolean isShowing() {
        return mIsResumed;
    }

    public boolean isOnDestroy() {
        return mIsDestroyed || isFinishing();
    }

    @Override
    protected void onDestroy() {
        //L.i(TAG, "BaseActivity >> onDestroy");
        //ModifyLoading.dismissBeforeOnDestroy(this);
        if (!DBBtUtil.isCollectionEmpty(mListReceiver)) {
            for (BroadcastReceiver br : mListReceiver) {
                unregisterReceiver(br);
            }
            mListReceiver.clear();
            mListReceiver = null;
        }

        if (!DBBtUtil.isCollectionEmpty(mListDialog)) {
            mListDialog.clear();
            mListDialog = null;
        }

        if (!DBBtUtil.isCollectionEmpty(mListPopupWindow)) {
            mListPopupWindow.clear();
            mListPopupWindow = null;
        }

        mListResultCallBack.clear();

        mHandler.removeCallbacksAndMessages(null);

        mIsDestroyed = true;
        mCurrFragment = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mCurrFragment != null && mCurrFragment.onBackPressed()) {
            return;
        }

        if (interceptBack()) {
            return;
        }

        super.onBackPressed();
    }

    private boolean interceptBack() {
        if (mListDialog != null && mListDialog.size() > 0) {
            for (Dialog dialog : mListDialog) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    return true;
                }
            }
        }

        if (mListPopupWindow != null && mListPopupWindow.size() > 0) {
            for (PopupWindow popupWindow : mListPopupWindow) {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCurrFragment != null && mCurrFragment.onKeyDown(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected Handler getHandler() {
        return mHandler;
    }

    public void postDelayed(final Runnable runnable, long delay) {
        if (mHandler != null) {
            stopLoop();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (runnable != null && !isOnDestroy())
                        runnable.run();
                }
            }, delay);
        }
    }

    public void post(Runnable runnable) {
        postDelayed(runnable, 0);
    }

    public void postLoop(int delay, final int interval, final Runnable runnable) {
        if (runnable != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    postDelayed(this, interval);
                }
            }, delay);
        }
    }

    public void stopLoop() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setStatusBarDarkMode(boolean darkmode) {

        //小米状态栏颜色改变
//        if ("xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
//            Class<? extends Window> clazz = getWindow().getClass();
//            try {
//                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
//                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
//                int darkModeFlag = field.getInt(layoutParams);
//                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
//                extraFlagField.invoke(getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else if (ActionBarProxy.hasSmartBar()) {
//            //魅族状态栏颜色改变
//            StatusBarProxy.setStatusBarDarkIcon(getWindow(), darkmode);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        //PermissionManager.getInstance().onRequestPermissionsResult(this, requestCode, grantResults);
//    }

    //简化布局里View的获取
    public <T extends View> T bindView(int resId) {
        return (T) findViewById(resId);
    }

    public <T extends View> T bindView(View view, int resId) {
        if (view == null) return null;
        return (T) view.findViewById(resId);
    }

    public <T extends View> T bindView(View view, int resId, View.OnClickListener listener) {
        T v = (T) view.findViewById(resId);
        if (v != null)
            v.setOnClickListener(listener);

        return v;
    }

    public <T extends View> T bindView(int resId, View.OnClickListener listener) {
        T v = (T) findViewById(resId);
        if (v != null)
            v.setOnClickListener(listener);

        return v;
    }

    public <T extends View> T bindView(int resId, View.OnClickListener listener, View.OnKeyListener keyListener) {
        T v = (T) findViewById(resId);
        if (v != null) {
            v.setOnClickListener(listener);
            v.setOnKeyListener(keyListener);
        }
        return v;
    }

    public <T extends View> T bindView(int resId, View.OnClickListener listener, View.OnLongClickListener longListener) {
        T t = bindView(resId, listener);
        if (t != null)
            t.setOnLongClickListener(longListener);

        return t;
    }

    public <T extends View> T bindView(int resId, boolean visible) {
        T v = (T) findViewById(resId);
        if (v != null)
            v.setVisibility(visible ? View.VISIBLE : View.GONE);

        return v;
    }

    public <T extends View> T bindText(int resId, String text) {
        T v = bindView(resId);
        if (v != null && v instanceof TextView) {
            ((TextView) v).setText(text);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (bundle != null && getIntent() != null && getIntent().getExtras() != null)
            bundle.putAll(getIntent().getExtras());
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ActivityResultCallBack callBack : mListResultCallBack) {
            callBack.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface ActivityResultCallBack {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
