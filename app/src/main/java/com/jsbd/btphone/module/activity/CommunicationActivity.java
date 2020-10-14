package com.jsbd.btphone.module.activity;


import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jsbd.btphone.R;
import com.jsbd.btphone.config.Config;
import com.jsbd.btphone.config.MainApp;
import com.jsbd.btphone.module.base.BaseActivity;
import com.jsbd.btphone.util.DBBtUtil;
import com.jsbd.btservice.bean.Device;
import com.jsbd.btservice.bean.HandsetCall;
import com.jsbd.support.bluetooth.BTController;
import com.jsbd.support.bluetooth.callback.IHfpCallback;
import com.jsbd.support.bluetooth.constant.BluetoothConstants;
import com.jsbd.support.bluetooth.utils.LogUtils;
import com.jsbd.support.bluetooth.utils.TextUtil;

public class CommunicationActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "CommunicationActivity";

    //来电界面
    private View mLayoutInComing;

    //通话或者呼出界面
    private View mLayoutSpeaking;

    //按键界面
    private View mLayoutKeyboard;

    private TextView mTvInCallName1;
    private TextView mTvInCallName2;

    private TextView mTvInCallNumber1;
    private TextView mTvInCallNumber2;

    private TextView mTvInCallDuration1;
    private TextView mTvInCallDuration2;

    //静音
    private ImageView mIvKeyMute1;
    private ImageView mIvKeyMute2;

    private ImageView mIvKeyPrivate2;

    private TextView mTvTelNumber;

    private long mClickTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "CommunicationActivity >> onCreate:" + hashCode() + ",thread:" + Thread.currentThread().getName());
        setContentView(R.layout.activity_in_call);
        init();
    }

    private void init() {
        initViews();
        initData();

        Intent it = new Intent(Config.ACTION_SERVICE_FLOAT_WINDOW);
        it.setComponent(new ComponentName(getPackageName(), "com.jsbd.btphone.service.CommunicationService"));
        it.setPackage(getPackageName());
        startService(it);

        if (BTController.getInstance().isBindService()) {
            updatePrivateMode(BTController.getInstance().getInstance().isPrivateMode());
            //进入通话界面，但是不是通话状态，直接finish
            if (!BTController.getInstance().getInstance().isCalling()) {
                finish();
            }
        }
    }

    private void initViews() {
        mLayoutInComing = bindView(R.id.incoming_layout);
        mLayoutSpeaking = bindView(R.id.outgoing_and_talking_layout);
        mLayoutKeyboard = bindView(R.id.include_keyboard_show);

        mTvInCallName1 = bindView(mLayoutInComing, R.id.tv_incall_user_name);
        mTvInCallName2 = bindView(mLayoutSpeaking, R.id.tv_incall_user_name);
        mTvInCallNumber1 = bindView(mLayoutInComing, R.id.tv_incall_user_number);
        mTvInCallNumber2 = bindView(mLayoutSpeaking, R.id.tv_incall_user_number);
        mTvInCallDuration1 = bindView(mLayoutInComing, R.id.tv_incall_duration);
        mTvInCallDuration2 = bindView(mLayoutSpeaking, R.id.tv_incall_duration);
        initKeyboard();
    }

    private void initKeyboard() {
        bindView(mLayoutInComing, R.id.iv_answer, this);

        bindView(mLayoutInComing, R.id.iv_hang_up, this);
        bindView(mLayoutSpeaking, R.id.iv_hang_up, this);

        mIvKeyMute1 = bindView(mLayoutInComing, R.id.iv_mute, this);
        mIvKeyMute2 = bindView(mLayoutSpeaking, R.id.iv_mute, this);

        bindView(mLayoutSpeaking, R.id.iv_keyboard, this);

        mIvKeyPrivate2 = bindView(mLayoutSpeaking, R.id.iv_private, this);

        mTvTelNumber = bindView(mLayoutKeyboard, R.id.keyboard_tv_telephone_number);

        bindView(mLayoutKeyboard, R.id.keyboard_key_one, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_two, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_three, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_four, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_five, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_six, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_seven, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_eight, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_nine, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_star, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_zero, this);
        bindView(mLayoutKeyboard, R.id.keyboard_key_pound, this);
        bindView(mLayoutKeyboard, R.id.keyboard_iv_delete, this);
        bindView(mLayoutKeyboard, R.id.keyboard_iv_hangup, this);
        bindView(mLayoutKeyboard, R.id.keyboard_iv_hide, this);
    }

    private void initData() {
        BTController.getInstance().registerHfpCallback(hashCode(), new IHfpCallback() {
            @Override
            public void onAudioStateChanged(int state) {
                LogUtils.d(TAG, "CommunicationActivity >> onAudioStateChanged >> state:" + state);
                updatePrivateMode(BTController.getInstance().getInstance().isPrivateMode());
            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                LogUtils.d(TAG, "CommunicationActivity >> onConnectStateChanged >> curState:" + curState);
                if (curState != BluetoothConstants.CONNECT_STATE_CONNECTED) {
                    finish();
                }
            }

            @Override
            public void onBatteryLevelChanged(int level, int max) {
            }

            @Override
            public void onSignalLevelChanged(int level, int max) {
            }

            @Override
            public void onCallChanged(int state, HandsetCall currCall, HandsetCall prevCall) {
                LogUtils.d(TAG, "CommunicationActivity >> onCallChanged >> state:" + state
                        + ",currCall=" + (currCall != null ? currCall.toString() : "")
                        + ",prevCall=" + (prevCall != null ? prevCall.toString() : ""));

                if (state == BluetoothConstants.CALL_STATE_INCOMING) {
                    updateStateLayout(1, currCall);
                } else if (state == BluetoothConstants.CALL_STATE_ACTIVE || state == BluetoothConstants.CALL_STATE_HELD) {
                    updateStateLayout(3, currCall);
                } else if (state == BluetoothConstants.CALL_STATE_TERMINATED) {
                    LogUtils.d(TAG, "CommunicationActivity >> onCallChanged >> finish");
                    finish();
                } else if (state == BluetoothConstants.CALL_STATE_DIALING || state == BluetoothConstants.CALL_STATE_ALERTING) {
                    updateStateLayout(2, currCall);
                } else if (state == BluetoothConstants.CALL_STATE_WAITING) {
                    if (currCall != null) {
                        if (!currCall.isOutgoing()) {
                            updateStateLayout(1, currCall);
                        } else {
                            updateStateLayout(2, currCall);
                        }
                    }
                }
            }

            @Override
            public void onHoldingTimeChanged(int callId, int holdingTime, String holdingTimeStr) {
                LogUtils.d(TAG, "CommunicationActivity >> onHoldingTimeChanged >> callId:" + callId + ",holdingTime:" + holdingTime + ",str:" + holdingTimeStr);
                HandsetCall currCall = BTController.getInstance().getInstance().getCurrentCall();
                if (currCall != null && currCall.getId() == callId) {
                    if (mTvInCallDuration2 != null) {
                        mTvInCallDuration2.setText(holdingTimeStr);
                    }
                }
            }
        });
    }

    /**
     * 刷新通话状态layout：来电界面/通话中界面/按键界面
     *
     * @param state
     * @param handsetCall
     */
    private void updateStateLayout(int state, HandsetCall handsetCall) {
        LogUtils.d(TAG, "CommunicationActivity >> updateStateLayout >> state:" + state);
        switch (state) {
            case 1://来电
                mLayoutInComing.setVisibility(View.VISIBLE);
                mLayoutSpeaking.setVisibility(View.GONE);
                mLayoutKeyboard.setVisibility(View.GONE);
                if (mLayoutInComing.getVisibility() == View.VISIBLE) {
                    HandsetCall call = BTController.getInstance().getInstance().getCurrentCall();
                    if (call != null) {
                        if (!TextUtil.isEmpty(call.getName())) {
                            mTvInCallName1.setText(call.getName());
                        } else {
                            mTvInCallName1.setText(R.string.bt_call_unknown_number_hint);
                        }
                        mTvInCallNumber1.setText(DBBtUtil.numberFormat(DBBtUtil.handleText(call.getNumber(), 15)));
                        mTvInCallDuration1.setText(R.string.bt_call_status_1);
                    }
                }
                break;
            case 2://去电
            case 3://通话中
                mLayoutSpeaking.setVisibility(View.VISIBLE);
                mLayoutInComing.setVisibility(View.GONE);
                mLayoutKeyboard.setVisibility(View.GONE);

                updatePrivateMode(BTController.getInstance().getInstance().isPrivateMode());

                if (handsetCall != null) {
                    if (!TextUtil.isEmpty(handsetCall.getName())) {
                        mTvInCallName2.setText(handsetCall.getName());
                    } else {
                        mTvInCallName2.setText(R.string.bt_call_unknown_number_hint);
                    }

                    mTvInCallNumber2.setText(DBBtUtil.numberFormat(DBBtUtil.handleText(handsetCall.getNumber(), 15)));
                    if (handsetCall.getState() == BluetoothConstants.CALL_STATE_DIALING || handsetCall.getState() == BluetoothConstants.CALL_STATE_ALERTING) {
                        mTvInCallDuration2.setText(R.string.bt_call_status_2);
                    } else {
                        mTvInCallDuration2.setText(handsetCall.getmHoldingTimeStr());
                    }
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "CommunicationActivity >> onResume:" + hashCode() + ",BTController.getInstance().getCallState():" + BTController.getInstance().getCallState());
        updateFloatView(Config.HIDE_FLOAT_WINDOW);

        /*在此处做判断当前是什么状态以便于显示相应界面*/
        HandsetCall currCall = BTController.getInstance().getInstance().getCurrentCall();
        switch (BTController.getInstance().getInstance().getCallState()) {
            case BluetoothConstants.CALL_STATE_INCOMING:
                updateStateLayout(1, currCall);
                break;
            case BluetoothConstants.CALL_STATE_DIALING:
            case BluetoothConstants.CALL_STATE_ALERTING:
                updateStateLayout(2, currCall);
                break;
            case BluetoothConstants.CALL_STATE_ACTIVE:
            case BluetoothConstants.CALL_STATE_HELD:
                updateStateLayout(3, currCall);
                break;
            case BluetoothConstants.CALL_STATE_WAITING:
                if (currCall != null) {
                    if (!currCall.isOutgoing()) {
                        updateStateLayout(1, currCall);
                    } else {
                        updateStateLayout(2, currCall);
                    }
                }
                break;
            default:
                updateStateLayout(3, currCall);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "CommunicationActivity >> onPause:" + hashCode());
        if (BTController.getInstance().getInstance().isCalling()) {
            updateFloatView(Config.SHOW_FLOAT_WINDOW, 400);
        }
    }

    @Override
    public void onClick(View view) {
        if (mLayoutInComing.getVisibility() == View.VISIBLE) {
            switch (view.getId()) {
                case R.id.iv_answer:
                    BTController.getInstance().accept();
                    break;
                case R.id.iv_hang_up:
                    BTController.getInstance().hangup();
                    break;
                case R.id.iv_mute:
                    BTController.getInstance().setSpeakerMute(!BTController.getInstance().isSpeakerMute());
                    mIvKeyMute1.setSelected(BTController.getInstance().isSpeakerMute());
                    break;
            }
        } else if (mLayoutSpeaking.getVisibility() == View.VISIBLE) {
            switch (view.getId()) {
                case R.id.iv_hang_up:
                    BTController.getInstance().hangup();
                    break;
                case R.id.iv_mute:
                    BTController.getInstance().setSpeakerMute(!BTController.getInstance().isSpeakerMute());
                    mIvKeyMute2.setSelected(BTController.getInstance().isSpeakerMute());
                    break;
                case R.id.iv_keyboard:
                    mLayoutKeyboard.setVisibility(View.VISIBLE);
                    mLayoutInComing.setVisibility(View.GONE);
                    mLayoutSpeaking.setVisibility(View.GONE);
                    break;
                case R.id.iv_private:
                    if (System.currentTimeMillis() - mClickTick < 500) {
                        break;
                    }

                    mClickTick = System.currentTimeMillis();
                    if (BTController.getInstance().isPrivateMode()) {
                        mIvKeyPrivate2.setSelected(false);
                        BTController.getInstance().setPrivateMode(false);
                    } else {
                        mIvKeyPrivate2.setSelected(true);
                        BTController.getInstance().setPrivateMode(true);
                    }

                    updateSpeakerMute();
                    break;
            }
        } else if (mLayoutKeyboard.getVisibility() == View.VISIBLE) {
            switch (view.getId()) {
                case R.id.keyboard_key_one:
                case R.id.keyboard_key_two:
                case R.id.keyboard_key_three:
                case R.id.keyboard_key_four:
                case R.id.keyboard_key_five:
                case R.id.keyboard_key_six:
                case R.id.keyboard_key_seven:
                case R.id.keyboard_key_eight:
                case R.id.keyboard_key_nine:
                case R.id.keyboard_key_star:
                case R.id.keyboard_key_zero:
                case R.id.keyboard_key_pound:
                    //注意及时刷新匹配列表
                    if (mTvTelNumber.getText().toString().replace(" ", "").length() >= 20) {
                        return;
                    }

                    String tag = view.getTag().toString().trim();
                    byte[] bytes = tag.getBytes();
                    BTController.getInstance().setDTMF(bytes[0]);
                    mTvTelNumber.append(tag);
                    break;
                case R.id.keyboard_iv_delete:
                    break;
                case R.id.keyboard_iv_hangup:
                    BTController.getInstance().hangup();
                    break;
                case R.id.keyboard_iv_hide:
                    mLayoutSpeaking.setVisibility(View.VISIBLE);
                    mLayoutInComing.setVisibility(View.GONE);
                    mLayoutKeyboard.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((MainApp) MainApp.getAppContext()).exitApp();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void updatePrivateMode(boolean isPrivateMode) {
        LogUtils.d(TAG, "CommunicationActivity >> updatePrivateMode >> isPrivateMode:" + isPrivateMode);
        if (mIvKeyPrivate2 != null) {
            mIvKeyPrivate2.setSelected(isPrivateMode);
        }

        updateSpeakerMute();
    }

    private void updateSpeakerMute() {
        boolean isSpeakerMute = BTController.getInstance().isSpeakerMute();
        boolean isPrivateMode = BTController.getInstance().isPrivateMode();
        LogUtils.d(TAG, "CommunicationActivity >> updateSpeakerMute >> isSpeakerMute:" + isSpeakerMute + ",isPrivateMode:" + isPrivateMode);
        if (isPrivateMode) {
            if (mIvKeyMute1 != null) {
                mIvKeyMute1.setEnabled(false);
            }

            if (mIvKeyMute2 != null) {
                mIvKeyMute2.setEnabled(false);
            }
        } else {
            if (mIvKeyMute1 != null) {
                mIvKeyMute1.setEnabled(true);
                mIvKeyMute1.setSelected(isSpeakerMute);
            }

            if (mIvKeyMute2 != null) {
                mIvKeyMute2.setEnabled(true);
                mIvKeyMute2.setSelected(isSpeakerMute);
            }
        }
    }

    /**
     * 使用粘性广播是防止接受端还没有注册就发送广播，导致接收器漏掉数据；
     * 场景：来电时候马上切到其他界面，导致Service端的Broadcast还没ready，就发送广播，导致接收器漏数据，没有显示通话悬浮窗。
     *
     * @param type
     * @param delay
     */
    private void updateFloatView(@Config.FloatWindowType int type, long delay) {
        LogUtils.d(TAG, "CommunicationActivity >> updateFloatView >> type:" + type + ",delay:" + delay);
        Intent intent = new Intent(Config.ACTION_BRROADCAST_FLOAT_WINDOW);
        intent.putExtra(Config.EXTRA_DATA_FLOAT_WINDOW, type);
        intent.putExtra(Config.EXTRA_DATA_DELAY_TIME, delay);
        getApplicationContext().sendStickyBroadcast(intent);
    }

    private void updateFloatView(@Config.FloatWindowType int type) {
        updateFloatView(type, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "CommunicationActivity >> onDestroy:" + hashCode());
        BTController.getInstance().unRegisterHfpCallback(hashCode());
    }
}
