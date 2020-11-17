package com.jsbd.btphone.module.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jsbd.bluetooth.BTController;
import com.jsbd.bluetooth.bean.CallLog;
import com.jsbd.bluetooth.bean.Contact;
import com.jsbd.bluetooth.bean.Device;
import com.jsbd.bluetooth.bean.HandsetCall;
import com.jsbd.bluetooth.callback.HfpCallback;
import com.jsbd.bluetooth.callback.PbapCallback;
import com.jsbd.bluetooth.constant.BluetoothConstants;
import com.jsbd.bluetooth.utils.LogUtils;
import com.jsbd.bluetooth.utils.TextUtil;
import com.jsbd.btphone.R;
import com.jsbd.btphone.module.base.LazyBaseFragment;
import com.jsbd.btphone.util.DBBtUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by QY on 2018/8/27.
 */

public class CallLogFragment extends LazyBaseFragment {
    private static final String TAG = "CallLogFragment";

    private ListView mLvCallLog;
    private CallLogAdapter mAdapterCallLog;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        LogUtils.d(TAG, "CallLogFragment >> onCreateViewLazy");
        setContentView(R.layout.fragment_bt_callhistory_layout);
        init();
    }

    private void init() {
        initViews();
        initData();
    }

    private void initViews() {
        mLvCallLog = bindView(R.id.rv_bt_callhistory);
        mAdapterCallLog = new CallLogAdapter(getActivity());
        mLvCallLog.setAdapter(mAdapterCallLog);

        mLvCallLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CallLog callLog = (CallLog) mAdapterCallLog.getItem(position);
                if (callLog != null) {
                    LogUtils.d(TAG, "CallLogFragment >> onItemClickListener >> callLog:" + callLog.getNumber());
                    BTController.getInstance().dial(callLog.getNumber());
                }
            }
        });
    }

    private void initData() {
        BTController.getInstance().registerPbapCallback(hashCode(), new PbapCallback() {

            @Override
            public void onPbapPowerStateChanged(boolean on) {

            }

            @Override
            public void onSyncStateChanged(int syncState, int syncType) {
                LogUtils.d(TAG, "CallLogFragment >> onSyncStateChanged >> syncState:" + syncState + ",getSyncType:" + BTController.getInstance().getSyncType());
                if (syncState == BluetoothConstants.SYNC_STATE_FINISHED) {
                    if (syncType == BluetoothConstants.SYNC_CALLLOG) {
                        mAdapterCallLog.setDataList(BTController.getInstance().getCallLogList());
                    }
                }
            }

            @Override
            public void onContactItemCountDetermined(int count) {

            }

            @Override
            public void onContactItemFetched(Contact contact) {

            }

            @Override
            public void onCallLogItemCountDetermined(int count) {

            }

            @Override
            public void onCallLogItemFetched(CallLog callLog) {

            }

            @Override
            public void onContactsCallback(List<Contact> list) {

            }
        });

        BTController.getInstance().registerHfpCallback(hashCode(), new HfpCallback() {

            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                LogUtils.d(TAG, "CallLogFragment >> onConnectStateChanged >> curState:" + curState);
                if (curState == BluetoothConstants.CONNECT_STATE_CONNECTED) {
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
                LogUtils.d(TAG, "CallLogFragment >> onCallChanged >>state:" + state);
            }

            @Override
            public void onHoldingTimeChanged(int callId, int holdingTime, String holdingTimeStr) {

            }
        });
    }

    public static String StrToDate(String str) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd");
        String result = dateFormat2.format(dateFormat.parse(str));
        return result;
    }

    public static String StrToTime(String str) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
        String result = dateFormat2.format(dateFormat.parse(str));
        return result;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.d(TAG, "CallLogFragment >> onHiddenChanged >> hidden:" + hidden);
        if (!hidden) {
            if (BTController.getInstance().isSyncing() && BTController.getInstance().getSyncType() == BluetoothConstants.SYNC_CALLLOG) {
                return;
            }

            mAdapterCallLog.setDataList(BTController.getInstance().getCallLogList());
            mLvCallLog.setSelection(0);

            BTController.getInstance().startSync(BluetoothConstants.SYNC_CALLLOG);
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        LogUtils.d(TAG, "CallLogFragment >> onDestroyViewLazy");
        BTController.getInstance().unRegisterPbapCallback(hashCode());
        BTController.getInstance().unRegisterHfpCallback(hashCode());
    }

    class CallLogAdapter extends BaseAdapter {
        private List<CallLog> mListCallLog = new ArrayList<>();
        private LayoutInflater mLayoutInflater;

        public CallLogAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        public void setDataList(List<CallLog> callLogList) {
            mListCallLog.clear();
            if (callLogList != null || callLogList.size() > 0) {
                mListCallLog.addAll(callLogList);
            }
            notifyDataSetChanged();
            LogUtils.d(TAG, "CallLogFragment >> CallLogAdapter >> setDataList >> size:" + mListCallLog.size());
        }

        public void addData(CallLog data) {
            if (data != null) {
                mListCallLog.add(0, data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "CallLogFragment >> CallLogAdapter >> addData >> size:" + mListCallLog.size());
            }
        }

        public void clearList() {
            mListCallLog.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mListCallLog.size();
        }

        @Override
        public Object getItem(int i) {
            return mListCallLog.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.bt_callhistory_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.tvCallLogType = (ImageView) convertView.findViewById(R.id.iv_callhistory_type);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_callhistory_user);
                viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_callhistory_date);
                viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_callhistory_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            CallLog callLog = (CallLog) getItem(position);
            viewHolder.tvName.setText("");
            viewHolder.tvDate.setText("");
            viewHolder.tvTime.setText("");
            int res = R.mipmap.bt_callhistory_item_dialed_numbers_image;
            switch (callLog.getType()) {
                case CallLog.CALLLOG_TYPE_INCOMING:
                    res = R.mipmap.bt_callhistory_item_receive_calls_image;
                    break;
                case CallLog.CALLLOG_TYPE_OUTGOING:
                    res = R.mipmap.bt_callhistory_item_dialed_numbers_image;
                    break;
                case CallLog.CALLLOG_TYPE_MISSED:
                    res = R.mipmap.bt_callhistory_item_missed_calls_image;
                    break;
                case CallLog.CALLLOG_TYPE_ALL:
                    break;
            }
            viewHolder.tvCallLogType.setImageResource(res);

            viewHolder.tvName.setText(DBBtUtil.handleText(
                    !TextUtil.isEmpty(callLog.getName()) ? callLog.getName() : callLog.getNumber(), 24));

            try {
                viewHolder.tvDate.setText(StrToDate(callLog.getDate()));
                viewHolder.tvTime.setText(StrToTime(callLog.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            private ImageView tvCallLogType;
            private TextView tvName;
            private TextView tvDate;
            private TextView tvTime;
        }
    }
}

