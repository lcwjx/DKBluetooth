package com.jsbd.btphone.module.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jsbd.btphone.R;
import com.jsbd.btphone.module.activity.PairedDevicesActivity;
import com.jsbd.btphone.module.base.LazyBaseFragment;
import com.jsbd.btphone.module.view.SwitchButton;
import com.jsbd.btphone.util.DBBtUtil;
import com.jsbd.btphone.util.LimitLengthFilter;
import com.jsbd.btservice.Device;
import com.jsbd.btservice.HandsetCall;
import com.jsbd.btservice.constant.BTConfig;
import com.jsbd.support.bluetooth.BTController;
import com.jsbd.support.bluetooth.callback.IGapCallback;
import com.jsbd.support.bluetooth.callback.IHfpCallback;
import com.jsbd.support.bluetooth.utils.LogUtils;
import com.jsbd.support.bluetooth.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QY on 2018/8/25.
 */

public class SettingFragment extends LazyBaseFragment {
    private static final String TAG = "SetFragment";

    private ListView mLvAvailableDevice;
    private TextView mTvBtName;
    private ImageView mIvDeviceConnPicture;
    private SwitchButton mBtnBtPowerSwitch;
    private ConstraintLayout mLayoutBtName;
    private ConstraintLayout mLayoutBtPairDevice;
    private ConstraintLayout mLayoutBtAvailableDevice;

    private DeviceAdapter mAdapterDevice;
    private TextView mTvCurPairDevice;
    private TextView mTvBtAvailableDeviceListStatus;

    public AlertDialog mDialog;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        LogUtils.d(TAG, "SettingFragment >> onCreateViewLazy");
        setContentView(R.layout.fragment_bt_set_layout);
        init();
    }

    private void init() {
        initViews();
        initData();
    }

    @Override
    protected void onBtServiceConnected() {
        super.onBtServiceConnected();
        handlerUpdateName(BTController.getInstance().getName());
    }

    private void initData() {
        BTController.getInstance().registerGapCallback(hashCode(), new IGapCallback() {
            @Override
            public void onBtNameChanged(String name) {
                handlerUpdateName(name);
            }

            @Override
            public void onPowerStateChanged(int state) {
                updatePowerState(state);
            }

            @Override
            public void onStartDiscovery() {
                LogUtils.d(TAG, "SettingFragment >> onStartDiscovery");
                /*开始搜索*/
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_available_device_list_status_scaning);
                mTvBtAvailableDeviceListStatus.setBackgroundResource(R.mipmap.bt_search_device_select_image);
                mAdapterDevice.clearList();
            }

            @Override
            public void onFinishDiscovery() {
                LogUtils.d(TAG, "SettingFragment >> onFinishDiscovery");
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_available_device_list_status_refresh);
                mTvBtAvailableDeviceListStatus.setBackgroundResource(R.mipmap.bt_search_device_image);

            }

            @Override
            public void onDiscoveryFound(Device device) {
                LogUtils.d(TAG, "SettingFragment >> onDiscoveryFound >> device:" + device.toString());
                mAdapterDevice.addData(device);
            }

            @Override
            public void onBondStateChanged(String address, int state) {
                LogUtils.d(TAG, "SettingFragment >> onBondStateChanged >>  address:  " + address +
                        " state:" + state);
                BTController.getInstance().requestAllDevices();
            }

            @Override
            public void onBondedDevices(List<Device> list) {

            }

            @Override
            public void onAllDevices(List<Device> list) {
                mAdapterDevice.setDataList(list);
            }

            /**
             * 显示配对码
             *
             * @param pairCode
             */
            @Override
            public void onPairingConfirmation(int pairCode) {
                LogUtils.d(TAG, "SettingFragment >> onPairingConfirmation >>pairCode:" + pairCode);
                /*由于改为全局改在广播监听该事件*/
//                BTController.getInstance().setPairingConfirmation();
            }

        });

        BTController.getInstance().registerHfpCallback(hashCode(), new IHfpCallback() {
            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                LogUtils.d(TAG, "SettingFragment >> onConnectStateChanged >> curState:" + curState);
                updateConnectInfo(curState, device);
//                mAdapterDevice.setDataList(BTController.getInstance().getAllDevices());
                BTController.getInstance().requestAllDevices();
            }

            @Override
            public void onBatteryLevelChanged(int level, int max) {

            }

            @Override
            public void onSignalLevelChanged(int level, int max) {

            }

            @Override
            public void onCallChanged(int state, HandsetCall currCall, HandsetCall prevCall) {

            }

            @Override
            public void onHoldingTimeChanged(int callId, int holdingTime, String holdingTimeStr) {

            }
        });
    }

    private void updatePowerState(int state) {
        LogUtils.d(TAG, "SettingFragment >> updatePowerState >> state:" + state);
        switch (state) {
            case BTConfig.STATE_ON://ON
                BTController.getInstance().setScanFilterType(BTConfig.SCAN_FILTER_PHONE);//设置可搜索设备类型
                mBtnBtPowerSwitch.setSwitch0n();
                mLayoutBtPairDevice.setVisibility(View.VISIBLE);
                mLayoutBtAvailableDevice.setVisibility(View.VISIBLE);
                //mTvBtAvailableDeviceListStatus.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                mTvBtAvailableDeviceListStatus.setBackgroundResource(R.mipmap.bt_search_device_image);
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_available_device_list_status_refresh);
                mLayoutBtAvailableDevice.setClickable(true);
                break;
            case BTConfig.STATE_OFF://OFF
                mBtnBtPowerSwitch.setSwitchOff();
                mTvCurPairDevice.setText("");
                //mTvBtAvailableDeviceListStatus.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                mTvBtAvailableDeviceListStatus.setBackgroundColor(Color.TRANSPARENT);
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_available_device_list_status_hint);
                mLayoutBtAvailableDevice.setClickable(false);
                mAdapterDevice.clearList();
                mLayoutBtPairDevice.setVisibility(View.GONE);
                mLayoutBtAvailableDevice.setVisibility(View.GONE);
                break;
            case BTConfig.STATE_TURNING_ON://ONING
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_swtich_oning);
                mTvBtAvailableDeviceListStatus.setBackgroundColor(Color.TRANSPARENT);
                break;
            case BTConfig.STATE_TURNING_OFF://OFFING
                mTvBtAvailableDeviceListStatus.setBackgroundColor(Color.TRANSPARENT);
                mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_swtich_offing);
                break;
        }
    }

    private void updateConnectInfo(int curState, Device device) {
        switch (curState) {
            case BTConfig.CONNECT_STATE_CONNECTED:
                mTvCurPairDevice.setVisibility(View.VISIBLE);
                mIvDeviceConnPicture.setVisibility(View.VISIBLE);
                if (device != null) {
                    mTvCurPairDevice.setText(TextUtil.isEmpty(device.getName()) ? device.getAddress() : device.getName());
                } else {
                    mTvCurPairDevice.setText(R.string.bt_unknown_device);
                }
                break;
            case BTConfig.CONNECT_STATE_CONNECTING:
            case BTConfig.CONNECT_STATE_DISCONNECTED:
            case BTConfig.CONNECT_STATE_DISCONNECTING:
                mTvCurPairDevice.setText("");
                mIvDeviceConnPicture.setVisibility(View.GONE);
                mTvCurPairDevice.setVisibility(View.GONE);
                break;
        }
        LogUtils.d(TAG, "SettingFragment >> updateConnectInfo >> connectState:" + BTController.getInstance().getConnectState() + ",curDevice:" + BTController.getInstance().getCurRemoteDevice());
    }

    private void initViews() {
        mLayoutBtName = (ConstraintLayout) findViewById(R.id.cl_bt_name);
        mLayoutBtPairDevice = (ConstraintLayout) findViewById(R.id.cl_bt_pair_device);
        mIvDeviceConnPicture = (ImageView) findViewById(R.id.iv_deiver_conn_pic);
        mLayoutBtAvailableDevice = (ConstraintLayout) findViewById(R.id.cl_bt_available_device);
        mTvBtName = (TextView) findViewById(R.id.tv_bt_name);
        mLayoutBtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });

        mBtnBtPowerSwitch = (SwitchButton) findViewById(R.id.bsb_bt_swtich);
        LogUtils.d(TAG, "SettingFragment >> initViews >> getState:" + BTController.getInstance().getState() + ",isSwitchStatus:" + mBtnBtPowerSwitch.isSwitchStatus());
        mBtnBtPowerSwitch.setOnMbClickListener(new SwitchButton.OnMClickListener() {
            @Override
            public void onClick(boolean isRight) {
                if (!BTController.getInstance().isEnable()) {
                    LogUtils.d(TAG, "Switch--ON");
                    BTController.getInstance().enable();
                    mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_swtich_oning);
                    mTvBtAvailableDeviceListStatus.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    LogUtils.d(TAG, "Switch--OFF");
                    BTController.getInstance().disable();
                    mTvBtAvailableDeviceListStatus.setText(R.string.bt_bt_swtich_offing);
                    mTvBtAvailableDeviceListStatus.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        mTvCurPairDevice = (TextView) findViewById(R.id.tv_cur_pair_device);
        if (BTController.getInstance().isConnected() && TextUtil.isEmpty(BTController.getInstance().getCurRemoteDevice().getName())) {
            LogUtils.d(TAG, "SettingFragment >> initViews >> isConnected:" + BTController.getInstance().isConnected() + ",name:" + BTController.getInstance().getCurRemoteDevice().getName());
            mTvCurPairDevice.setText(BTController.getInstance().getCurRemoteDevice().getName());
        }

        mLayoutBtPairDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PairedDevicesActivity.class));
            }
        });

        /* 可用列表状态*/
        mTvBtAvailableDeviceListStatus = (TextView) findViewById(R.id.tv_available_device_list_stauts);
        mLayoutBtAvailableDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BTController.getInstance().isDiscovering()) {
                    BTController.getInstance().cancelDiscovery();
                } else {
                    BTController.getInstance().startDiscovery(30);
                }
            }
        });

        mAdapterDevice = new DeviceAdapter(getActivity());
        /*可用列表*/
        mLvAvailableDevice = (ListView) findViewById(R.id.bt_available_device_list);
        mLvAvailableDevice.setAdapter(mAdapterDevice);
        mLvAvailableDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                LogUtils.d(TAG, "SettingFragment >> onItemClick:" + BTController.getInstance().isConnected());
                BTController.getInstance().cancelDiscovery();
                if (BTController.getInstance().isConnected()) {
                    if (BTController.getInstance().getCurRemoteDevice() != null) {
                        LogUtils.d(TAG, "SettingFragment >> onItemClick >> disconnect >>name:" + BTController.getInstance().getCurRemoteDevice().getName());
                    }
                    connDeviceDialog(R.string.bt_set_disconnect_device_hint, BTController.getInstance().getCurRemoteDevice());
                } else {
                    Device device = (Device) mAdapterDevice.getItem(position);
                    LogUtils.d(TAG, "SettingFragment >> onItemClick >> connect >> name:" + device.getName());
                    connDeviceDialog(R.string.bt_set_connection_device_hint, device);
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            LogUtils.d(TAG, "SettingFragment >> onHiddenChanged >> show");
//            mAdapterDevice.setDataList(BTController.getInstance().getAllDevices());
            BTController.getInstance().requestAllDevices();
            updateConnectInfo(BTController.getInstance().getConnectState(), BTController.getInstance().getCurRemoteDevice());
        } else {
            LogUtils.d(TAG, "SettingFragment >> onHiddenChanged >> hide");
            if (BTController.getInstance().isDiscovering()) {
                BTController.getInstance().cancelDiscovery();
            }

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    @Override
    protected void onResumeLazy() {
        super.onResumeLazy();
        LogUtils.d(TAG, "SettingFragment >> onResumeLazy");
    }

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
        LogUtils.d(TAG, "SettingFragment >> onPauseLazy");
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();

        BTController.getInstance().unRegisterHfpCallback(hashCode());
        BTController.getInstance().unRegisterGapCallback(hashCode());
        LogUtils.d(TAG, "SettingFragment >> onDestroyViewLazy");
    }

    private void connDeviceDialog(int strID, final Device device) {
        if (device == null) return;

        LogUtils.d(TAG, "SettingFragment >> connDeviceDialog:" + mDialog);
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(getActivity(), R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.bt_set_connection_device_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_set_connection_device_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        mDialog.setCanceledOnTouchOutside(false);

        TextView dialogHint = (TextView) mDialog.findViewById(R.id.dialoghint);
        dialogHint.setText(strID);

        Button btnOK = (Button) mDialog.findViewById(R.id.bt_ok);
        Button btnCancel = (Button) mDialog.findViewById(R.id.bt_cancel);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (BTController.getInstance().isConnected() && BTController.getInstance().getCurRemoteDevice() != null) {
                    BTController.getInstance().disconnect(BTController.getInstance().getCurRemoteDevice().getAddress());
                    LogUtils.d(TAG, "SettingFragment >> disconnect-->" + BTController.getInstance().getCurRemoteDevice().getAddress());
                } else {
                    BTController.getInstance().connect(device.getAddress());
                    LogUtils.d(TAG, "SettingFragment >> connect-->" + device.getAddress());
                }
                mDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDialog.dismiss();
            }
        });
    }

    private void handlerUpdateName(String name) {
        if (mTvBtName != null) {
            LogUtils.d(TAG, "SettingFragment >> handlerUpdateName >> getName:" + BTController.getInstance().getName() + ",name:" + name);
            mTvBtName.setText(name);
        }
    }

    private void showAlertDialog() {
        String btName = BTController.getInstance().getName();
        if (TextUtil.isEmpty(btName)) {
            return;
        }

        if (!BTController.getInstance().isEnable()) {
            return;
        }

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(getActivity(), R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.bt_set_name_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_set_name_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        Button btnOK = (Button) mDialog.findViewById(R.id.bt_ok);
        Button btnCancel = (Button) mDialog.findViewById(R.id.bt_cancel);
        Button btnDelete = (Button) mDialog.findViewById(R.id.bt_delete);
        final EditText editContent = (EditText) mDialog.findViewById(R.id.et_name);

        LimitLengthFilter.setLimitLength(editContent);

        editContent.setText(btName);
        editContent.setSelection(editContent.getText().length());
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                editContent.setText("");
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = editContent.getText().toString();
                if (BTController.getInstance().setName(str)) {
                    mDialog.dismiss();
                } else {
                    editContent.setText("");
                    editContent.setHint(R.string.bt_set_name_null_prompt);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDialog.dismiss();
            }
        });
    }

    class DeviceAdapter extends BaseAdapter {
        private List<Device> mDataList = new ArrayList<>();
        private LayoutInflater mLayoutInflater;

        public DeviceAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public void setDataList(List<Device> dataList) {
            mDataList.clear();
            if (dataList != null && dataList.size() > 0) {
                for (Device device : dataList) {
                    if (!device.isConnected()) {
                        if (device.getBondState() == BTConfig.BOND_STATE_BONDING || device.isBonded()) {
                            mDataList.add(0, device);
                        } else {
                            mDataList.add(device);
                        }
                    }
                }
            }

            notifyDataSetChanged();
            LogUtils.d(TAG, "SettingFragment >> DeviceAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(Device device) {
            if (device != null && !device.isConnected()) {
                mDataList.add(0, device);
                notifyDataSetChanged();
                LogUtils.d(TAG, "SettingFragment >> DeviceAdapter >> addData >> size:" + mDataList.size());
            }
        }

        public void clearList() {
            mDataList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.bt_device_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_device_name);
                viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.tv_device_status);
                viewHolder.ivDevice = (ImageView) convertView.findViewById(R.id.iv_device_pic);
                viewHolder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_device_delete_button);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Device dev = (Device) getItem(position);
            String name = dev.getAliasName();
            String addr = dev.getAddress();

            //LogUtils.d(TAG, "SettingFragment >> DeviceAdapter >> getView >> pos:" + position + ",dev:" + dev.toString() + ",name=" + DBBtUtil.handleText(name, 20));
            viewHolder.tvName.setText(DBBtUtil.handleText(TextUtil.isEmpty(name) ? addr : name, 20));
            viewHolder.ivDelete.setVisibility(View.GONE);

            if (dev.isConnected()) {
                viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                viewHolder.tvStatus.setText(R.string.bt_bt_pair_device_status_connected);
                viewHolder.ivDevice.setSelected(true);

            } else if (dev.getBondState() == BTConfig.BOND_STATE_BONDED) {
                viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.tvStatus.setText(R.string.bt_bt_pair_device_status_paired);
                viewHolder.ivDevice.setSelected(false);

            } else if (dev.getBondState() == BTConfig.BOND_STATE_BONDING) {
                viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                viewHolder.tvStatus.setText(R.string.bt_bt_device_status_connecting);
                viewHolder.ivDevice.setSelected(true);

            } else {
                viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.tvStatus.setText(R.string.bt_bt_device_status_not_onnected);
                viewHolder.ivDevice.setSelected(false);
            }
            viewHolder.ivDevice.setImageResource(R.drawable.bt_set_device_pic_image);

            return convertView;
        }

        private class ViewHolder {
            private TextView tvName;
            private TextView tvStatus;
            private ImageView ivDelete;
            private ImageView ivDevice;
        }
    }
}
