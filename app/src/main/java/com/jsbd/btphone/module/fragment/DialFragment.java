package com.jsbd.btphone.module.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jsbd.btphone.R;
import com.jsbd.btphone.bean.ContactNumber;
import com.jsbd.btphone.module.base.LazyBaseFragment;
import com.jsbd.btphone.util.DBBtUtil;
import com.jsbd.btservice.bean.CallLog;
import com.jsbd.btservice.bean.Contact;
import com.jsbd.btservice.bean.Device;
import com.jsbd.btservice.bean.HandsetCall;
import com.jsbd.support.bluetooth.BTController;
import com.jsbd.support.bluetooth.callback.IHfpCallback;
import com.jsbd.support.bluetooth.callback.IPbapCallback;
import com.jsbd.support.bluetooth.constant.BluetoothConstants;
import com.jsbd.support.bluetooth.utils.LogUtils;
import com.jsbd.support.bluetooth.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QY on 2018/8/27.
 */

public class DialFragment extends LazyBaseFragment implements View.OnClickListener {
    private static final String TAG = "DialFragment";

    private TextView mTxtTelPhoneNumber;
    private ListView mListContacts;

    private DialContactAdapter mAdapter;
    private AlertDialog mDialog = null;
    private NumberAdapter mNumberAdapter;
    private long mClickTick;
    private String mLastDialNumber;
    private String number;

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        LogUtils.d(TAG, "DialFragment >> onCreateViewLazy");
        setContentView(R.layout.fragment_bt_dial_layout);
        init();
    }

    private void init() {
        initViews();
        initData();
    }

    private void initData() {
        BTController.getInstance().registerHfpCallback(hashCode(), new IHfpCallback() {
            @Override
            public void onAudioStateChanged(int state) {

            }

            @Override
            public void onConnectStateChanged(int curState, int prevState, Device device) {
                if (curState == BluetoothConstants.CONNECT_STATE_DISCONNECTED) {
                    mLastDialNumber = "";
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

            }

            @Override
            public void onHoldingTimeChanged(int id, int holdingTime, String holdingTimeStr) {

            }
        });

        BTController.getInstance().registerPbapCallback(hashCode(), new IPbapCallback() {
            @Override
            public void onPbapPowerStateChanged(boolean b) {

            }

            @Override
            public void onSyncStateChanged(int i, int i1) {

            }


            @Override
            public void onContactItemCountDetermined(int i) {

            }

            @Override
            public void onContactItemFetched(Contact contact) {

            }

            @Override
            public void onCallLogItemCountDetermined(int i) {

            }

            @Override
            public void onCallLogItemFetched(CallLog callLog) {

            }

            @Override
            public void onContactsCallback(List<Contact> list) {
                mAdapter.setDataList(list, number);
            }
        });
    }

    private void initViews() {
        mListContacts = bindView(R.id.recycler_dial_pair_contact_list);
        mAdapter = new DialContactAdapter(getActivity());
        mListContacts.setAdapter(mAdapter);

        mTxtTelPhoneNumber = bindView(R.id.tv_telephone_number);
        bindView(R.id.key_one, this);
        bindView(R.id.key_two, this);
        bindView(R.id.key_three, this);
        bindView(R.id.key_four, this);
        bindView(R.id.key_five, this);
        bindView(R.id.key_six, this);
        bindView(R.id.key_seven, this);
        bindView(R.id.key_eight, this);
        bindView(R.id.key_nine, this);
        bindView(R.id.key_star, this);
        bindView(R.id.key_pound, this);
        bindView(R.id.iv_call, this);
        ImageView keyDelete = bindView(R.id.iv_delete, this);
        ImageView keyZero = bindView(R.id.key_zero, this);

        keyZero.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mTxtTelPhoneNumber.getText().toString().replace(" ", "").length() <= 20) {
                    input("+");
                }
                return true;
            }
        });

        keyDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTxtTelPhoneNumber.setText("");
                mListContacts.setSelection(0);
                return true;
            }
        });

        mTxtTelPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                number = text.replace(" ", "");
//                mAdapter.setDataList(queryContactsByCondition(number), number);
                queryContactsByCondition(number);
                mListContacts.setSelection(0);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    if (i != 3 && i != 8 && text.charAt(i) == ' ') {
                        continue;
                    } else {
                        sb.append(text.charAt(i));
                        if ((sb.length() == 4 || sb.length() == 9) && sb.charAt(sb.length() - 1) != ' ') {
                            sb.insert(sb.length() - 1, ' ');
                        }
                    }
                }

                if (!sb.toString().equals(text)) {
                    mTxtTelPhoneNumber.setText(sb.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void createContactDialog(String user, List<ContactNumber> mList) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(getActivity(), R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.bt_contact_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_contact_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        TextView tv_curr_user = (TextView) mDialog.findViewById(R.id.tv_curr_user);
        tv_curr_user.setText(user);
        mNumberAdapter = new NumberAdapter();

        ListView contactNumberListView = (ListView) mDialog.findViewById(R.id.lv_contact_number_list);
        contactNumberListView.setAdapter(mNumberAdapter);
        mNumberAdapter.setDataList(mList);

        contactNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ContactNumber contactNumber = (ContactNumber) mNumberAdapter.getItem(i);
                LogUtils.d(TAG, "DialFragment >> createContactDialog >> getNumber:" + contactNumber.getNumber());
                if (contactNumber.getNumber() != null) {
                    BTController.getInstance().dial(contactNumber.getNumber().replace(" ", ""));
                    mTxtTelPhoneNumber.setText("");
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
        });
    }

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
        LogUtils.d(TAG, "DialFragment >> onPauseLazy");
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.d(TAG, "DialFragment >> onHiddenChanged >> hidden:" + hidden);
        if (!hidden) {
            if (mTxtTelPhoneNumber != null) {
                number = mTxtTelPhoneNumber.getText().toString().replace(" ", "");
//                mAdapter.setDataList(queryContactsByCondition(input), input);
                queryContactsByCondition(number);
            }
        }
    }

    private void input(String str) {
        String p = mTxtTelPhoneNumber.getText().toString().replace(" ", "");
        mTxtTelPhoneNumber.setText((p + str).replace(" ", ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.key_one:
            case R.id.key_two:
            case R.id.key_three:
            case R.id.key_four:
            case R.id.key_five:
            case R.id.key_six:
            case R.id.key_seven:
            case R.id.key_eight:
            case R.id.key_nine:
            case R.id.key_star:
            case R.id.key_zero:
            case R.id.key_pound:
                //注意及时刷新匹配列表
                if (mTxtTelPhoneNumber.getText().toString().replace(" ", "").length() >= 20)
                    return;

                input(v.getTag().toString());
                break;
            case R.id.iv_delete:
                String p = mTxtTelPhoneNumber.getText().toString().replace(" ", "");
                mListContacts.setSelection(0);
                if (p.length() > 0) {
                    mTxtTelPhoneNumber.setText(p.substring(0, p.length() - 1));
                }
                break;
            case R.id.iv_call:
                if (mTxtTelPhoneNumber.getText().toString().length() > 0) {
                    if (System.currentTimeMillis() - mClickTick < 500) {
                        break;
                    }

                    mLastDialNumber = mTxtTelPhoneNumber.getText().toString().replace(" ", "");
                    dial(mLastDialNumber);
                } else {
                    if (!TextUtil.isEmpty(mLastDialNumber)) {
                        mTxtTelPhoneNumber.setText(mLastDialNumber);
                    } else {
                        if (System.currentTimeMillis() - mClickTick < 500) {
                            break;
                        }

                        BTController.getInstance().reDial();
                    }
                }

                mClickTick = System.currentTimeMillis();
                break;
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
        BTController.getInstance().unRegisterHfpCallback(hashCode());
        LogUtils.d(TAG, "DialFragment >> onDestroyViewLazy");
    }

    private void queryContactsByCondition(String input) {
        BTController.getInstance().queryContactsByCondition(input);
    }

    private void dial(String number) {
        if (TextUtil.isEmpty(number)) return;
        BTController.getInstance().dial(number.replace(" ", ""));
        mTxtTelPhoneNumber.setText("");
    }

    class NumberAdapter extends BaseAdapter {
        private List<ContactNumber> mDataList = new ArrayList<>();

        public void setDataList(List<ContactNumber> dataList) {
            mDataList.clear();
            if (dataList != null && dataList.size() > 0) {
                mDataList.addAll(dataList);
            }
            notifyDataSetChanged();
            LogUtils.d(TAG, "DialFragment >> NumberAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(ContactNumber data) {
            if (data != null) {
                mDataList.add(data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "DialFragment >> NumberAdapter >> addData >> size:" + mDataList.size());
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
            if (null == convertView) {
                convertView = View.inflate(getApplicationContext(), R.layout.bt_contact_dialog_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.numberType = (TextView) convertView.findViewById(R.id.tv_number_type);
                viewHolder.matchNumber = (TextView) convertView.findViewById(R.id.tv_match_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ContactNumber contactNumber = (ContactNumber) getItem(position);
            //LogUtils.d(TAG, "DialFragment >> NumberAdapter >> getView >> contactNumber:" + contactNumber.toString());

            viewHolder.numberType.setText("");
            viewHolder.matchNumber.setText("");
            int numberType = 0;
            switch (contactNumber.getNumberType()) {
                case NUMBER_TYPE_CELL:
                    numberType = R.string.bt_contact_numbertype_1;
                    break;
                case NUMBER_TYPE_WORK:
                    numberType = R.string.bt_contact_numbertype_2;
                    break;
                case NUMBER_TYPE_HOME:
                    numberType = R.string.bt_contact_numbertype_3;
                    break;
                case NUMBER_TYPE_OTHER:
                    numberType = R.string.bt_contact_numbertype_4;
                    break;
            }
            viewHolder.numberType.setText(numberType);
            viewHolder.matchNumber.setText(DBBtUtil.numberFormat(DBBtUtil.handleText(contactNumber.getNumber(), 15)));
            //LogUtils.d(TAG, "DialFragment >> NumberAdapter >> getView >> numberType:" + numberType + ",number:" + contactNumber.getNumber());

            return convertView;
        }

        private class ViewHolder {
            private TextView numberType;
            private TextView matchNumber;
        }
    }

    class DialContactAdapter extends BaseAdapter {
        private List<Contact> mDataList = new ArrayList<>();
        private String inputStr = null;
        private int highlightColor = Color.parseColor("#18E2E5");

        private LayoutInflater mLayoutInflater;

        public DialContactAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            highlightColor = context.getResources().getColor(R.color.bt_font_stytle_one);
        }

        public void setDataList(List<Contact> dataList, String inputStr) {
            this.inputStr = inputStr;
            mDataList.clear();
            if (dataList != null && dataList.size() != 0) {
                mDataList.addAll(dataList);
            }

            notifyDataSetChanged();
            LogUtils.d(TAG, "DialFragment >> DialAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(Contact data) {
            if (data != null) {
                mDataList.add(data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "DialFragment >> DialAdapter >> addData >> size:" + mDataList.size());
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
            if (null == convertView) {
                convertView = mLayoutInflater.inflate(R.layout.bt_dial_pair_contact_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.contactLayout = (ConstraintLayout) convertView.findViewById(R.id.cl_bt_dial_pair_contact_item);
                viewHolder.name = (TextView) convertView.findViewById(R.id.iv_dail_pair_contact_name);
                viewHolder.number = (TextView) convertView.findViewById(R.id.tv_dial_pair_contact_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = (Contact) getItem(position);
            if (!TextUtil.isEmpty(contact.getName())) {
                viewHolder.name.setText(DBBtUtil.handleText(contact.getName(), 12));
            }

            final List<ContactNumber> mList = new ArrayList<>();
            if (contact.hasCellNumbers()) {
                for (String list : contact.getCellNumbers()) {
                    mList.add(new ContactNumber(Contact.NumberType.NUMBER_TYPE_CELL, list));
                }
            }

            if (contact.hasWorkNumbers()) {
                for (String list : contact.getWorkNumbers()) {
                    mList.add(new ContactNumber(Contact.NumberType.NUMBER_TYPE_WORK, list));
                }
            }

            if (contact.hasHomeNumbers()) {
                for (String list : contact.getHomeNumbers()) {
                    mList.add(new ContactNumber(Contact.NumberType.NUMBER_TYPE_HOME, list));
                }
            }

            if (contact.hasOtherNumbers()) {
                for (String list : contact.getOtherNumbers()) {
                    mList.add(new ContactNumber(Contact.NumberType.NUMBER_TYPE_OTHER, list));
                }
            }

            for (int i = 0; i < mList.size(); i++) {
                String number = mList.get(i).getNumber();
                if (!TextUtil.isEmpty(number)) {
                    String formatNumber = DBBtUtil.numberFormat(DBBtUtil.handleText(number, 15));
                    if (!TextUtil.isEmpty(inputStr)) {
                        if (number.contains(inputStr)) {
                            viewHolder.number.setText(DBBtUtil.parserHighLightText(formatNumber, inputStr, highlightColor));
                            if (TextUtil.isEmpty(contact.getName())) {
                                viewHolder.name.setText(DBBtUtil.handleText(formatNumber, 12));
                            }
                            break;
                        }
                    } else {
                        viewHolder.number.setText(formatNumber);
                        if (TextUtil.isEmpty(contact.getName())) {
                            viewHolder.name.setText(DBBtUtil.handleText(formatNumber, 12));
                        }
                        break;
                    }
                }
            }

            viewHolder.contactLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mList.size() > 1) {
                        createContactDialog(contact.getName(), mList);
                    } else if (mList.size() == 1) {
                        if (mList.get(0).getNumber() != null && mList.get(0).getNumber().length() > 0) {
                            BTController.getInstance().dial(mList.get(0).getNumber());
                            mTxtTelPhoneNumber.setText("");
                        }
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            private TextView name;
            private TextView number;
            private ConstraintLayout contactLayout;
        }
    }
}
