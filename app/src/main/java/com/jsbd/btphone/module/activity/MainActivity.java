package com.jsbd.btphone.module.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jsbd.bluetooth.BTController;
import com.jsbd.bluetooth.bean.Device;
import com.jsbd.bluetooth.bean.HandsetCall;
import com.jsbd.bluetooth.callback.HfpCallback;
import com.jsbd.bluetooth.callback.IHfpCallback;
import com.jsbd.bluetooth.constant.BluetoothConstants;
import com.jsbd.bluetooth.observer.HfpObserver;
import com.jsbd.bluetooth.utils.LogUtils;
import com.jsbd.btphone.R;
import com.jsbd.btphone.config.Config;
import com.jsbd.btphone.module.base.BaseActivity;
import com.jsbd.btphone.module.base.BaseFragment;
import com.jsbd.btphone.module.fragment.CallLogFragment;
import com.jsbd.btphone.module.fragment.ContactsFragment;
import com.jsbd.btphone.module.fragment.DialFragment;
import com.jsbd.btphone.module.fragment.SettingFragment;
import com.jsbd.btphone.module.view.NoScrollViewPager;
import com.jsbd.btphone.util.PermissionRequestUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";

    public static final String CURRENT_FRAGMENT = "current_fragment";

    private static final int HANDLER_FRAGMENT_SET = 100001;
    private static final int HANDLER_FRAGMENT_DIAL = 100002;
    private static final int HANDLER_FRAGMENT_CALLLOG = 100003;
    private static final int HANDLER_FRAGMENT_CONTACT = 100004;

    private NoScrollViewPager mVpFragments;
    private RadioButton mRBtnSetting;
    private RadioButton mRBtnKeyboard;
    private RadioButton mRBtnCallLog;
    private RadioButton mRBtnContact;
    private RadioGroup mRGroupTab;

    private List<BaseFragment> mListFragment = new ArrayList<>();

    @Config.PageType
    private int mCurPage = Config.PAGE_IDLE;

    private final WeakHandler mHandler = new WeakHandler(this);

    private static class WeakHandler extends Handler {
        private final WeakReference<MainActivity> mWeakReference;

        public WeakHandler(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mWeakReference.get();
            if (activity == null) {
                super.handleMessage(msg);
                return;
            }

            switch (msg.what) {
                case HANDLER_FRAGMENT_SET:
                    LogUtils.d(TAG, "MainActivity >> handleMessage >> HANDLER_FRAGMENT_SET");
                    activity.mRGroupTab.check(R.id.rb_bt_set);
                    break;
                case HANDLER_FRAGMENT_DIAL:
                    LogUtils.d(TAG, "MainActivity >> handleMessage >> HANDLER_FRAGMENT_DIAL");
                    activity.mRGroupTab.check(R.id.rb_bt_dial);
                    break;
                case HANDLER_FRAGMENT_CALLLOG:
                    LogUtils.d(TAG, "MainActivity >> handleMessage >> HANDLER_FRAGMENT_CALLLOG");
                    activity.mRGroupTab.check(R.id.rb_bt_calllog);
                    break;
                case HANDLER_FRAGMENT_CONTACT:
                    LogUtils.d(TAG, "MainActivity >> handleMessage >> HANDLER_FRAGMENT_CONTACT");
                    activity.mRGroupTab.check(R.id.rb_bt_contact);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        LogUtils.d(TAG, "MainActivity >> onCreate:" + hashCode());
    }

    private void init() {
        initViews();
        initData();
    }

    private void initViews() {
        initFragments();

        mVpFragments = bindView(R.id.vp_viewpage);
        mRBtnSetting = bindView(R.id.rb_bt_set);
        mRBtnKeyboard = bindView(R.id.rb_bt_dial);
        mRBtnCallLog = bindView(R.id.rb_bt_calllog);
        mRBtnContact = bindView(R.id.rb_bt_contact);
        mRGroupTab = bindView(R.id.rg_bt_tab_list);

        mRGroupTab.setOnCheckedChangeListener(this);
        mRGroupTab.check(R.id.rb_bt_set);

        mVpFragments.setNoScroll(true);
        mVpFragments.setAdapter(new BluetoothPagerAdapter(getSupportFragmentManager(), mListFragment));
        //设置预加载页数
        mVpFragments.setOffscreenPageLimit(4);
        //mVpFragments.setCurrentItem(0, false);
        mVpFragments.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mHandler.removeMessages(HANDLER_FRAGMENT_SET);
                        mHandler.removeMessages(HANDLER_FRAGMENT_DIAL);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CALLLOG);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CONTACT);
                        mHandler.sendEmptyMessageDelayed(HANDLER_FRAGMENT_SET, 200);
                        break;
                    case 1:
                        mHandler.removeMessages(HANDLER_FRAGMENT_SET);
                        mHandler.removeMessages(HANDLER_FRAGMENT_DIAL);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CALLLOG);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CONTACT);
                        mHandler.sendEmptyMessageDelayed(HANDLER_FRAGMENT_DIAL, 200);
                        break;
                    case 2:
                        mHandler.removeMessages(HANDLER_FRAGMENT_SET);
                        mHandler.removeMessages(HANDLER_FRAGMENT_DIAL);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CALLLOG);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CONTACT);
                        mHandler.sendEmptyMessageDelayed(HANDLER_FRAGMENT_CALLLOG, 200);
                        break;
                    case 3:
                        mHandler.removeMessages(HANDLER_FRAGMENT_SET);
                        mHandler.removeMessages(HANDLER_FRAGMENT_DIAL);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CALLLOG);
                        mHandler.removeMessages(HANDLER_FRAGMENT_CONTACT);
                        mHandler.sendEmptyMessageDelayed(HANDLER_FRAGMENT_CONTACT, 200);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        switchPage(getIntent().getIntExtra(CURRENT_FRAGMENT, Config.PAGE_SETTING));
    }

    private void initData() {
        initAllNeededPermissions();

        registerFinishReceiver(Config.ACTION_EXIT_APP);

        if (BTController.getInstance().isBindService()) {

            getLocalVersionName();
            updatePowerState(BTController.getInstance().getConnectState() == BluetoothConstants.CONNECT_STATE_CONNECTED);
            updateConnectState(BTController.getInstance().getConnectState(), BTController.getInstance().getCurRemoteDevice());

            LogUtils.d(TAG, "MainActivity >> bindToService >> isCalling:" + BTController.getInstance().isCalling() + ",state:" + BTController.getInstance().getState());
            if (BTController.getInstance().isCalling()) {
                Intent intent = new Intent(MainActivity.this, CommunicationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        BTController.getInstance().registerHfpCallback(hashCode(), new HfpCallback() {
            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                updateConnectState(curState, device);
            }

            @Override
            public void onBatteryLevelChanged(int level, int max) {

            }

            @Override
            public void onSignalLevelChanged(int level, int max) {

            }

            @Override
            public void onCallChanged(int state, HandsetCall currCall) {
                LogUtils.d(TAG, "MainActivity >> onCallChanged:" + state);
            }
        });
    }

    private void initFragments() {
        mListFragment.add(new SettingFragment());
        mListFragment.add(new DialFragment());
        mListFragment.add(new CallLogFragment());
        mListFragment.add(new ContactsFragment());
    }

    private void initAllNeededPermissions() {
        PermissionRequestUtils.requestMultiPermissions(MainActivity.this,
                new int[]{PermissionRequestUtils.CODE_LOCATION, PermissionRequestUtils.CODE_CONTACTS, PermissionRequestUtils.CODE_PHONE},
                new PermissionRequestUtils.PermissionGrant() {
                    @Override
                    public void onPermissionGranted(int requestCode) {
//                        L.i(TAG, "onPermissionGranted ,requestCode :" + requestCode);
                    }
                });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_bt_set:
                switchPage(Config.PAGE_SETTING);
                break;
            case R.id.rb_bt_dial:
                switchPage(Config.PAGE_DIAL);
                break;
            case R.id.rb_bt_calllog:
                switchPage(Config.PAGE_CALLLOG);
                break;
            case R.id.rb_bt_contact:
                switchPage(Config.PAGE_CONTACT);
                break;
        }
    }

    private void switchPage(@Config.PageType int page) {
        if (mCurPage != page) {
            if (page != Config.PAGE_SETTING) {
                if (!BTController.getInstance().isConnected()) {
                    page = Config.PAGE_SETTING;
                }
            }
            mCurPage = page;
            setCurrFragment(mListFragment.get(page));
            mVpFragments.setCurrentItem(page, false);
        }
    }

    private void updateConnectState(int curState, Device device) {
        switch (curState) {
            case BluetoothConstants.CONNECT_STATE_CONNECTED:
                updatePowerState(true);
                break;
            case BluetoothConstants.CONNECT_STATE_CONNECTING:
            case BluetoothConstants.CONNECT_STATE_DISCONNECTED:
            case BluetoothConstants.CONNECT_STATE_DISCONNECTING:
                updatePowerState(false);
                break;
        }
    }

    private void updatePowerState(boolean on) {
        setTabBtnEnable(!on);
        if (!on) {
            switchPage(Config.PAGE_SETTING);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        switchPage(getIntent().getIntExtra(CURRENT_FRAGMENT, mCurPage));

        //通话过程中需要覆盖通话界面
        if (BTController.getInstance().isCalling()) {
            Intent intent1 = new Intent(MainActivity.this, CommunicationActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }
    }

    @Override
    public void onResume() {
        //通话过程中需要覆盖通话界面
        if (BTController.getInstance().isCalling()) {
            Intent intent = new Intent(MainActivity.this, CommunicationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        super.onResume();
        updateConnectState(BTController.getInstance().getConnectState(), BTController.getInstance().getCurRemoteDevice());
        LogUtils.d(TAG, "MainActivity >> onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "MainActivity >> onPause");
    }

    private void setTabBtnEnable(boolean enable) {
        LogUtils.d(TAG, "MainActivity >> setTabBtnEnable >> enable:" + enable);
        mRBtnSetting.setEnabled(true);
        mRBtnSetting.setBackground(getResources().getDrawable(
                enable ? R.drawable.bt_tab_set_image : R.drawable.bt_tab_set_highlight_image));

        if (enable) {
            mRBtnSetting.setChecked(true);
        }

        mRBtnKeyboard.setEnabled(!enable);
        mRBtnKeyboard.setBackground(getResources().getDrawable(
                enable ? R.drawable.bt_tab_dial_image : R.drawable.bt_tab_dial_highlight_image));

        mRBtnCallLog.setEnabled(!enable);
        mRBtnCallLog.setBackground(getResources().getDrawable(
                enable ? R.drawable.bt_tab_callhistory_image : R.drawable.bt_tab_callhistory_highlight_image));

        mRBtnContact.setEnabled(!enable);
        mRBtnContact.setBackground(getResources().getDrawable(
                enable ? R.drawable.bt_tab_contact_image : R.drawable.bt_tab_contact_highlight_image));
    }

    public String getLocalVersionName() {
        String localVersion = "";
        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionName;
            LogUtils.d(TAG, "MainActivity >> bindToService >> BT Version:" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return localVersion;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "MainActivity >> onDestroy:" + hashCode());
        mHandler.removeCallbacksAndMessages(null);

        HfpObserver.getInstance().unRegister(hashCode());
    }

    private static class BluetoothPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> mFragmentList;

        public BluetoothPagerAdapter(FragmentManager fm, List<BaseFragment> fragmentList) {
            super(fm);
            this.mFragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }
}
