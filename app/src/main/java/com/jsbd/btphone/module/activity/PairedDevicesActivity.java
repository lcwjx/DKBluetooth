package com.jsbd.btphone.module.activity;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jsbd.bluetooth.BTController;
import com.jsbd.bluetooth.bean.Device;
import com.jsbd.bluetooth.bean.HandsetCall;
import com.jsbd.bluetooth.callback.GapCallback;
import com.jsbd.bluetooth.callback.HfpCallback;
import com.jsbd.bluetooth.constant.BluetoothConstants;
import com.jsbd.bluetooth.utils.LogUtils;
import com.jsbd.bluetooth.utils.TextUtil;
import com.jsbd.btphone.R;
import com.jsbd.btphone.util.DBBtUtil;

import java.util.ArrayList;
import java.util.List;

public class PairedDevicesActivity extends AppCompatActivity {
    private static final String TAG = "PairedDevicesActivity";

    private DeviceAdapter mAdapterDevice;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "PairedDevicesActivity >> onCreate");
        setContentView(R.layout.activity_bt_pair_device);
        init();
    }

    private void init() {
        initViews();
        initData();
    }

    private void initData() {
        BTController.getInstance().registerGapCallback(hashCode(), new GapCallback() {
            @Override
            public void onBtNameChanged(String name) {

            }

            @Override
            public void onPowerStateChanged(int state) {
                updatePowerState(state);
            }

            @Override
            public void onStartDiscovery() {
            }

            @Override
            public void onFinishDiscovery() {
//                mAdapterDevice.setDataList(BTController.getInstance().getBondedDevices());
                BTController.getInstance().requestBondedDevices();
            }

            @Override
            public void onDiscoveryFound(Device device) {
            }

            @Override
            public void onBondStateChanged(Device device, int state) {
                LogUtils.d(TAG, "PairedDevicesActivity >> onBondStateChanged >> state:" + state);
//                mAdapterDevice.setDataList(BTController.getInstance().getBondedDevices());
                BTController.getInstance().requestBondedDevices();
            }

            @Override
            public void onBondedDevices(List<Device> list) {
                mAdapterDevice.setDataList(list);
            }

            @Override
            public void onAllDevices(List<Device> list) {

            }

            /**
             * 显示配对码
             *
             * @param pairCode
             */
            @Override
            public void onPairingConfirmation(int pairCode) {
                LogUtils.d(TAG, "PairedDevicesActivity >> onPairingConfirmation:" + pairCode);
            }

        });

        BTController.getInstance().registerHfpCallback(hashCode(), new HfpCallback() {
            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                LogUtils.d(TAG, "PairedDevicesActivity >> onConnectStateChanged >> curState:" + curState);
//                mAdapterDevice.setDataList(BTController.getInstance().getBondedDevices());
                BTController.getInstance().requestBondedDevices();
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

    private void initViews() {
        mAdapterDevice = new DeviceAdapter();
        ListView lvPairDevice = (ListView) findViewById(R.id.bt_pair_device_list);
        lvPairDevice.setAdapter(mAdapterDevice);

        lvPairDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Device device = (Device) mAdapterDevice.getItem(position);
                if (BTController.getInstance().isConnected()) {
                    if (BTController.getInstance().getCurRemoteDevice() != null) {
                        LogUtils.d(TAG, "PairedDevicesActivity >> onItemClick >> disconnect >>name:" + BTController.getInstance().getCurRemoteDevice().getName());
                    }
                    createConnDeviceDialog(R.string.bt_set_disconnect_device_hint, BTController.getInstance().getCurRemoteDevice());
                } else {
                    LogUtils.d(TAG, "PairedDevicesActivity >> onItemClick >> connect >> name:" + device.getName());
                    createConnDeviceDialog(R.string.bt_set_connection_device_hint, device);
                }
            }
        });
    }

    private void updatePowerState(int state) {
        switch (state) {
            case BluetoothConstants.STATE_OFF://OFF
            case BluetoothConstants.STATE_TURNING_ON://ONING
            case BluetoothConstants.STATE_TURNING_OFF://OFFING
                finish();
                break;
        }
    }

    private void removeDeviceDialog(int resId, final Device device) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(this, R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(this).inflate(R.layout.bt_set_connection_device_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_set_connection_device_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());

        TextView dialogHint = (TextView) mDialog.findViewById(R.id.dialoghint);
        dialogHint.setText(resId);

        Button btnOK = (Button) mDialog.findViewById(R.id.bt_ok);
        Button btnCancel = (Button) mDialog.findViewById(R.id.bt_cancel);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                BTController.getInstance().removeBond(device.getAddress());
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

    private void createConnDeviceDialog(int resId, final Device device) {
        if (device == null) return;

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(this, R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(this).inflate(R.layout.bt_set_connection_device_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_set_connection_device_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());

        TextView dialogHint = (TextView) mDialog.findViewById(R.id.dialoghint);
        dialogHint.setText(resId);

        Button btnOK = (Button) mDialog.findViewById(R.id.bt_ok);
        Button btnCancel = (Button) mDialog.findViewById(R.id.bt_cancel);
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (BTController.getInstance().isConnected() && BTController.getInstance().getCurRemoteDevice() != null) {
                    BTController.getInstance().disconnect(BTController.getInstance().getCurRemoteDevice().getAddress());
                    LogUtils.d(TAG, "PairedDevicesActivity >> disconnect >> address:" + BTController.getInstance().getCurRemoteDevice().getAddress());
                } else {
                    BTController.getInstance().connect(device.getAddress());
                    LogUtils.d(TAG, "PairedDevicesActivity >> connect >> address:" + device.getAddress());
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

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "PairedDevicesActivity >> onResume");
//        mAdapterDevice.setDataList(BTController.getInstance().getBondedDevices());
        BTController.getInstance().requestBondedDevices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "PairedDevicesActivity >> onPause");
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "PairedDevicesActivity >> onDestroy");
        BTController.getInstance().unRegisterHfpCallback(hashCode());
        BTController.getInstance().unRegisterGapCallback(hashCode());
    }

    class DeviceAdapter extends BaseAdapter {
        private List<Device> mDataList = new ArrayList<>();

        public void setDataList(List<Device> dataList) {
            mDataList.clear();
            if (dataList != null && dataList.size() > 0) {
                mDataList.addAll(dataList);
            }
            notifyDataSetChanged();
            LogUtils.d(TAG, "PairedDevicesActivity >> DeviceAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(Device data) {
            if (data != null) {
                mDataList.add(0, data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "PairedDevicesActivity >> DeviceAdapter >> addData >> size:" + mDataList.size());
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
            if (i >= mDataList.size()) {
                return null;
            }

            return mDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.bt_device_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_device_name);
                viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.tv_device_status);
                viewHolder.ivDevice = (ImageView) convertView.findViewById(R.id.iv_device_pic);
                viewHolder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_device_delete_button);

                viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getItem(position) != null) {
                            removeDeviceDialog(R.string.bt_set_remove_device_hint, (Device) getItem(position));
                        }
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Device dev = (Device) getItem(position);
            LogUtils.d(TAG, "PairedDevicesActivity >> DeviceAdapter >> getItem >> pos:" + position + ",dev:" + dev.toString());
            if (dev != null) {
                String name = dev.getName();
                String address = dev.getAddress();

                viewHolder.tvName.setText(DBBtUtil.handleText(TextUtil.isEmpty(name) ? address : name, 20));
                viewHolder.ivDevice.setImageResource(R.drawable.bt_set_device_pic_image);
                viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_set_available_device_item_a50_stytle));
                viewHolder.ivDelete.setVisibility(View.GONE);

                if (dev.getBondState() == BluetoothConstants.BOND_STATE_BONDED) {
                    if (dev.isConnected()) {
                        viewHolder.ivDevice.setSelected(true);
                        viewHolder.tvName.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                        viewHolder.tvStatus.setTextColor(getResources().getColor(R.color.bt_font_stytle_one));
                        viewHolder.tvStatus.setVisibility(View.VISIBLE);
                        viewHolder.tvStatus.setText(R.string.bt_bt_pair_device_status_connected);
                    } else {
                        viewHolder.ivDevice.setSelected(false);
                        viewHolder.tvStatus.setVisibility(View.GONE);
                        viewHolder.ivDelete.setVisibility(View.VISIBLE);
                    }
                }
            }

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
