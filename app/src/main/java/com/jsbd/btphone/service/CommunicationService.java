package com.jsbd.btphone.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jsbd.bluetooth.BTController;
import com.jsbd.bluetooth.bean.Device;
import com.jsbd.bluetooth.bean.HandsetCall;
import com.jsbd.bluetooth.callback.HfpCallback;
import com.jsbd.bluetooth.constant.BluetoothConstants;
import com.jsbd.bluetooth.observer.HfpObserver;
import com.jsbd.bluetooth.utils.LogUtils;
import com.jsbd.bluetooth.utils.TextUtil;
import com.jsbd.btphone.R;
import com.jsbd.btphone.config.Config;
import com.jsbd.btphone.config.WeakHandler;
import com.jsbd.btphone.module.activity.CommunicationActivity;
import com.jsbd.btphone.receiver.MediaButtonReceiver;

/**
 * Created by wills on 2017/12/18.
 * 通话悬浮窗service.
 */
public class CommunicationService extends Service {

    private static final String TAG = "CommunicationService";

    private final static int MESSAGE_SHOW_FLOAT_VIEW = 1;
    private final static int MESSAGE_HIDE_FLOAT_VIEW = 2;
    private final static int MESSAGE_DESTROY_FLOAT_VIEW = 3;

    //悬浮窗高度
    private final static int HEIGHT_FLOAT_VIEW = 72;

    private WindowManager.LayoutParams mWindowParams;
    private TextView mFloatView;

    private boolean mDestroyed = false;
    private boolean mIsShowFloatView = false;

    private AudioManager mAudioManager;
    private ComponentName mComponentName;

    private FloatHandler mFloatHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "CommunicationService >> onCreate");
        mDestroyed = false;

        registerReceivers();
    }

    private void registerReceivers() {
        HfpObserver.getInstance().register(hashCode(), new HfpCallback() {

            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                LogUtils.d(TAG, "CommunicationService >> onConnectStateChanged >> curState:" + curState);
                if (curState != BluetoothConstants.CONNECT_STATE_CONNECTED) {
                    destroyFloatView(0);
                }
            }

            @Override
            public void onBatteryLevelChanged(int level, int max) {

            }

            @Override
            public void onSignalLevelChanged(int level, int max) {

            }

            @Override
            public void onCallChanged(int state, HandsetCall currCall) {
                LogUtils.d(TAG, "CommunicationService >> onCallChanged:" + state);
                if (state == BluetoothConstants.CALL_STATE_TERMINATED || state == BluetoothConstants.CALL_STATE_IDLE) {
                    handleDestroyFloatView();
                    stopSelf();
                }
            }
        });

        mComponentName = new ComponentName(this, MediaButtonReceiver.class);
        mAudioManager = ((AudioManager) getSystemService(AUDIO_SERVICE));

        mAudioManager.registerMediaButtonEventReceiver(mComponentName);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.ACTION_BRROADCAST_FLOAT_WINDOW);
        intentFilter.addAction(Config.ACTION_BRROADCAST_MEDIA_PLAY);
        intentFilter.addAction(Config.ACTION_BRROADCAST_MEDIA_PAUSE);
        intentFilter.addAction(Config.ACTION_BRROADCAST_MEDIA_NEXT);
        intentFilter.addAction(Config.ACTION_BRROADCAST_MEDIA_PREVIOUS);
        intentFilter.addAction(Config.ACTION_BRROADCAST_MEDIA_PLAY_PAUSE);
        registerReceiver(mReceiver, intentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtil.isEmpty(action)) return;

            LogUtils.d(TAG, "CommunicationService >> onReceive >> action:" + action);
            if (action.equals(Config.ACTION_BRROADCAST_FLOAT_WINDOW)) {
                int type = intent.getIntExtra(Config.EXTRA_DATA_FLOAT_WINDOW, 0);
                long delay = intent.getLongExtra(Config.EXTRA_DATA_DELAY_TIME, 0);
                switch (type) {
                    case Config.HIDE_FLOAT_WINDOW:
                        hideFloatView(delay);
                        break;
                    case Config.SHOW_FLOAT_WINDOW:
                        showFloatView(delay);
                        break;
                    case Config.DESTROY_FLOAT_WINDOW:
                        destroyFloatView(delay);
                        break;
                }
            } else if (action.equals(Config.ACTION_BRROADCAST_MEDIA_PLAY)) {
            } else if (action.equals(Config.ACTION_BRROADCAST_MEDIA_PAUSE)) {
            } else if (action.equals(Config.ACTION_BRROADCAST_MEDIA_PLAY_PAUSE)) {
            } else if (action.equals(Config.ACTION_BRROADCAST_MEDIA_NEXT)) {
                //面板按键下一曲表示挂断
                BTController.getInstance().hangup();
            } else if (action.equals(Config.ACTION_BRROADCAST_MEDIA_PREVIOUS)) {
                //面板按键上一曲表示接听
                BTController.getInstance().accept();
            }
        }
    };

    private void createFloatView() {
        if (mWindowParams == null) {
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            // 设置图片格式，效果为背景透明
            mWindowParams.format = PixelFormat.RGBA_8888;
            // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWindowParams.gravity = Gravity.TOP;
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            mWindowParams.x = 0;
            mWindowParams.y = 0;

            // 设置悬浮窗口长宽数据
            mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mWindowParams.height = HEIGHT_FLOAT_VIEW;
        }

        if (mFloatView == null) {
            // 获取浮动窗口视图所在布局
            mFloatView = new TextView(getApplication());
            mFloatView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            mFloatView.setTextColor(Color.WHITE);
            mFloatView.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
            mFloatView.setTextSize(24);
            mFloatView.setGravity(Gravity.CENTER);

            mFloatView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CommunicationService.this, CommunicationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "CommunicationService >> onStartCommand");
        mDestroyed = false;
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        mDestroyed = false;
        LogUtils.d(TAG, "CommunicationService >> onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.d(TAG, "CommunicationService >> onUnbind");
        return super.onUnbind(intent);
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        HfpObserver.getInstance().unRegister(hashCode());

        if (mAudioManager != null && mComponentName != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
        }

        unregisterReceiver(mReceiver);
        LogUtils.d(TAG, "CommunicationService >> onDestroy");
    }

    public boolean isDestroyed() {
        return mDestroyed;
    }

    /**
     * 显示悬浮窗
     */
    private void handleShowFloatView() {
        LogUtils.d(TAG, "CommunicationService >> handleShowFloatView");
        createFloatView();
        if (!mIsShowFloatView) {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (mFloatView != null && mWindowParams != null) {
                try {
                    windowManager.addView(mFloatView, mWindowParams);
                    // set BT_SHOW_STATUS_BAR SHOW
//                    CommunicationService.getInstance().getCarServiceProxy().setCMSStatus(CMSStatusFuc.BT_SHOW_STATUS_BAR, BTShowStatusBar.STATUS_SHOW, false);
                    mIsShowFloatView = true;

                    HandsetCall currCall = BTController.getInstance().getCurrentCall();
                    if (currCall != null) {
                        handleUpdateHoldingTime("00:00:00");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 隐藏悬浮窗
     */
    private void handleHideFloatView() {
        LogUtils.d(TAG, "CommunicationService >> handleHideFloatView");
        if (mIsShowFloatView) {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (mFloatView != null) {
                try {
                    windowManager.removeView(mFloatView);
                    mIsShowFloatView = false;
                    // set BT_SHOW_STATUS_BAR HIDE
//                    BTService.getInstance().getCarServiceProxy().setCMSStatus(CMSStatusFuc.BT_SHOW_STATUS_BAR, BTShowStatusBar.STATUS_HIDE, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 销毁悬浮窗
     */
    private void handleDestroyFloatView() {
        LogUtils.d(TAG, "CommunicationService >> handleDestroyFloatView");
        handleHideFloatView();

        if (mFloatHandler != null) {
            mFloatHandler.removeCallbacksAndMessages(null);
            mFloatHandler = null;
        }

        if (mFloatView != null) {
            mFloatView.setOnClickListener(null);
            mFloatView = null;
        }

        mWindowParams = null;
    }

    /**
     * 更新通话时间
     */
    private void handleUpdateHoldingTime(String time) {
        if (mFloatView != null) {
            LogUtils.d(TAG, "handleUpdateHoldingTime >> time:" + time);
            mFloatView.setText(getString(R.string.float_window_tip_touch_back_bt) + time);
            mFloatView.setVisibility(View.VISIBLE);
        }
    }

    public void showFloatView(long delay) {
        LogUtils.d(TAG, "CommunicationService >> showFloatView");
        if (mFloatHandler == null) {
            mFloatHandler = new FloatHandler(this);
        }

        mFloatHandler.removeMessages(MESSAGE_SHOW_FLOAT_VIEW);
        mFloatHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_FLOAT_VIEW, delay);
    }

    public void hideFloatView(long delay) {
        LogUtils.d(TAG, "CommunicationService >> hideFloatView");
        if (mFloatHandler != null) {
            mFloatHandler.removeMessages(MESSAGE_HIDE_FLOAT_VIEW);
            mFloatHandler.removeMessages(MESSAGE_SHOW_FLOAT_VIEW);
            mFloatHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_FLOAT_VIEW, delay);
        }
    }

    public void destroyFloatView(long delay) {
        LogUtils.d(TAG, "CommunicationService >> destroyFloatView");
        if (mFloatHandler != null) {
            mFloatHandler.removeMessages(MESSAGE_DESTROY_FLOAT_VIEW);
            mFloatHandler.removeMessages(MESSAGE_SHOW_FLOAT_VIEW);
            mFloatHandler.removeMessages(MESSAGE_HIDE_FLOAT_VIEW);
            mFloatHandler.sendEmptyMessageDelayed(MESSAGE_DESTROY_FLOAT_VIEW, delay);
        }
    }

    private static class FloatHandler extends WeakHandler<CommunicationService> {
        public FloatHandler(CommunicationService who) {
            super(who);
        }

        @Override
        public void onHandleMessage(CommunicationService self, Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_FLOAT_VIEW:
                    self.handleShowFloatView();
                    break;
                case MESSAGE_HIDE_FLOAT_VIEW:
                    self.handleHideFloatView();
                    break;
                case MESSAGE_DESTROY_FLOAT_VIEW:
                    self.handleDestroyFloatView();
                    break;
            }
        }
    }
}
