package com.jsbd.btphone.module.fragment;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.jsbd.btphone.R;
import com.jsbd.btphone.bean.ContactNumber;
import com.jsbd.btphone.config.WeakHandler;
import com.jsbd.btphone.module.base.LazyBaseFragment;
import com.jsbd.btphone.module.view.ProgressDialog;
import com.jsbd.btphone.util.DBBtUtil;
import com.jsbd.btphone.util.SoftInputUtil;
import com.jsbd.btservice.bean.CallLog;
import com.jsbd.btservice.bean.Contact;
import com.jsbd.support.bluetooth.BTController;
import com.jsbd.support.bluetooth.callback.IPbapCallback;
import com.jsbd.support.bluetooth.constant.BluetoothConstants;
import com.jsbd.support.bluetooth.utils.LogUtils;
import com.jsbd.support.bluetooth.utils.TextUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by QY on 2018/8/25.
 */

public class ContactsFragment extends LazyBaseFragment {
    private static final String TAG = "ContactsFragment";

    private static final int HANDLER_WHAT_DISMISS_PROGRESS_DIALOG = 1;
    private static final int HANDLER_WHAT_DISMISS_TIP_DIALOG = 2;

    private ListView mLvContacts;
    private ContactAdapter mAdapterContact;
    private NumberAdapter mAdapterNumber;
    private EditText mEdtSearch;
    private ProgressDialog mDlgProgress = null;
    private AlertDialog mDialog = null;

    private boolean mIsNeedShowTipDlg = false;
    private DelayHandler mHandler = null;

    private static class DelayHandler extends WeakHandler<ContactsFragment> {

        private DelayHandler(ContactsFragment fragment) {
            super(fragment);
        }

        @Override
        public void onHandleMessage(ContactsFragment self, Message msg) {
            switch (msg.what) {
                case HANDLER_WHAT_DISMISS_PROGRESS_DIALOG:
                    self.stopProgressDialog();
                    break;
                case HANDLER_WHAT_DISMISS_TIP_DIALOG:
                    self.dismissTipDialog();
                    break;
            }
        }
    }

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        LogUtils.d(TAG, "ContactsFragment >> onCreateViewLazy");
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_bt_contact_layout);
        init();
    }

    private void init() {
        initViews();
        initData();
    }

    private void initViews() {
        mEdtSearch = bindView(R.id.et_search);

        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    SoftInputUtil.hideSoftInput(mEdtSearch);
                    search();
                }
                return true;
            }
        });

        mEdtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                String number = text.replace(" ", "");
                if (TextUtil.isEmpty(number)) {
                    mAdapterContact.setDataList(BTController.getInstance().getContactList());
                } else {
                    BTController.getInstance().queryContactsByCondition(number);
                }
                mLvContacts.setSelection(0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        bindView(R.id.bt_search, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d(TAG, "ContactsFragment >> onClick >> BtnSearch");
                SoftInputUtil.hideSoftInput(mEdtSearch);
                search();
            }
        });

        bindView(R.id.bt_sync, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtils.d(TAG, "ContactsFragment >> BtnSync OnClick");
                if (BTController.getInstance().isPbapConnected()) {
                    if (BTController.getInstance().startSync(BluetoothConstants.SYNC_CONTACT)) {
                        startProgressDialog();
                    } else {
                        if (BTController.getInstance().getSyncType() == BluetoothConstants.SYNC_CALLLOG && BTController.getInstance().isSyncing())
                            createTipDialog(getResources().getString(R.string.bt_pbap_downloading_calllog));
                    }
                } else {
                    if (BTController.getInstance().getCurRemoteDevice() != null) {
                        BTController.getInstance().connectPBAP(BTController.getInstance().getCurRemoteDevice());
                    }
                    stopProgressDialog();
                    mIsNeedShowTipDlg = true;
                    createTipDialog(getResources().getString(R.string.bt_pbap_conn_prompt));
                }
            }
        });

        mAdapterContact = new ContactAdapter();
        mLvContacts = bindView(R.id.rv_bt_cantact_list);
        mLvContacts.setAdapter(mAdapterContact);

        mLvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SoftInputUtil.hideSoftInput(mEdtSearch);
                Contact contact = (Contact) mAdapterContact.getItem(position);
                LogUtils.d(TAG, "ContactsFragment >> onItemClick >> contact:" + contact.toString());
            }
        });

        mLvContacts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                SoftInputUtil.hideSoftInput(mEdtSearch);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }

    private void initData() {
        mHandler = new DelayHandler(this);

        mIsNeedShowTipDlg = false;

        BTController.getInstance().registerPbapCallback(hashCode(), new IPbapCallback() {
            @Override
            public void onPbapPowerStateChanged(boolean on) {
                if (on) {
                    if (isShowing() && mIsNeedShowTipDlg) {
                        createTipDialog(getResources().getString(R.string.bt_pbap_conn_success));
                    }

                    mIsNeedShowTipDlg = false;
                }
            }

            @Override
            public void onSyncStateChanged(int syncState, int syncType) {
                LogUtils.d(TAG, "ContactsFragment >> onSyncStateChanged state:" + syncState + ",syncType:" + BTController.getInstance().getSyncType());
                if (syncState == BluetoothConstants.SYNC_STATE_STARTED) {
                    if (syncType == BluetoothConstants.SYNC_CONTACT) {
                        LogUtils.d(TAG, "ContactsFragment >> onSyncStateChanged SYNC_STATE_STARTED");
                        if (isShowing()) {
                            startProgressDialog();
                        }
                        mAdapterContact.clearList();
                    }
                } else if (syncState == BluetoothConstants.SYNC_STATE_FINISHED) {
                    if (syncType == BluetoothConstants.SYNC_CONTACT) {
                        LogUtils.d(TAG, "ContactsFragment >> onSyncStateChanged SYNC_STATE_FINISHED");
                        mAdapterContact.setDataList(BTController.getInstance().getContactList());
                        finishProgressDialog();
                    }
                }
            }


            @Override
            public void onContactItemCountDetermined(int count) {
            }

            @Override
            public void onContactItemFetched(final Contact bluetoothContact) {
                if (mDlgProgress != null) {
                    mDlgProgress.setLoading(BTController.getInstance().getContactSize());
                }
            }

            @Override
            public void onCallLogItemCountDetermined(int count) {

            }

            @Override
            public void onCallLogItemFetched(CallLog bluetoothCallLog) {

            }

            @Override
            public void onContactsCallback(List<Contact> list) {
                mAdapterContact.setDataList(list);
            }
        });
    }


    private void search() {
        String input = mEdtSearch.getText().toString().trim();
        if (TextUtil.isEmpty(input)) {
            mAdapterContact.setDataList(BTController.getInstance().getContactList());
        } else {
            BTController.getInstance().queryContactsByCondition(input);
        }
    }

    private void startProgressDialog() {
        if (mDlgProgress == null) {
            mDlgProgress = new ProgressDialog(getActivity());
            mDlgProgress.setMessage(R.string.bt_contacts_sync_loading_status);
            mDlgProgress.show();
            LogUtils.d(TAG, "ContactFragment >> startProgressDialog");
        }
    }

    private void finishProgressDialog() {
        if (BTController.getInstance().isPbapConnected()) {
            if (mDlgProgress != null) {
                mDlgProgress.setComplete(BTController.getInstance().getContactSize());
                LogUtils.d(TAG, "ContactFragment >> finishProgressDialog >> complete");
                mHandler.removeMessages(HANDLER_WHAT_DISMISS_PROGRESS_DIALOG);
                mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_DISMISS_PROGRESS_DIALOG, 2000);
            }
        } else {
            stopProgressDialog();
        }
    }

    public void stopProgressDialog() {
        if (mDlgProgress != null) {
            LogUtils.d(TAG, "ContactFragment >> stopProgressDialog");
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
    }

    private void createTipDialog(String str) {
        dismissTipDialog();

        mDialog = new AlertDialog.Builder(getActivity(), R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.bt_set_connection_device_code_dialog_layout, null));
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); //需要设置AlertDialog的类型，保证在广播接收者中可以正常弹出
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//隐藏键盘
        mDialog.getWindow().setContentView(R.layout.bt_set_connection_device_code_dialog_layout);
        mDialog.getWindow().setGravity(Gravity.CENTER | Gravity.TOP);
        mDialog.getWindow().setDimAmount(0f);
        mDialog.setCanceledOnTouchOutside(false);

        ((TextView) mDialog.findViewById(R.id.dialog_paircode)).setText(str);

        mHandler.removeMessages(HANDLER_WHAT_DISMISS_TIP_DIALOG);
        mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_DISMISS_TIP_DIALOG, 5000);
    }

    private void createContactDialog(String user, List<ContactNumber> mList) {
        dismissTipDialog();

        mDialog = new AlertDialog.Builder(getActivity(), R.style.Transparent).create();
        mDialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.bt_contact_dialog_layout, null));
        mDialog.show();

        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDialog.getWindow().setContentView(R.layout.bt_contact_dialog_layout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        ((TextView) mDialog.findViewById(R.id.tv_curr_user)).setText(user);

        mAdapterNumber = new NumberAdapter();
        ListView contactNumberListView = (ListView) mDialog.findViewById(R.id.lv_contact_number_list);
        contactNumberListView.setAdapter(mAdapterNumber);
        mAdapterNumber.setDataList(mList);

        contactNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ContactNumber mContactNumber = (ContactNumber) mAdapterNumber.getItem(i);
                LogUtils.d(TAG, "ContactsFragment >> ContactsDialog >> onItemClick >> number:" + mContactNumber.getNumber());
                if (mContactNumber.getNumber() != null) {
                    BTController.getInstance().dial(mContactNumber.getNumber().replace(" ", ""));
                    dismissTipDialog();
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtils.d(TAG, "ContactsFragment >> onHiddenChanged >> hidden:" + hidden);
        if (!hidden) {
            if (BTController.getInstance().getSyncType() == BluetoothConstants.SYNC_CONTACT && BTController.getInstance().isSyncing()) {
                startProgressDialog();
            } else {
                if (TextUtil.isEmpty(mEdtSearch.getText().toString())) {
                    mAdapterContact.setDataList(BTController.getInstance().getContactList());
                } else {
                    BTController.getInstance().queryContactsByCondition(mEdtSearch.getText().toString());
                }
                mLvContacts.setSelection(0);
            }
        } else {
            SoftInputUtil.hideSoftInput(mEdtSearch);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (BTController.getInstance().isSyncing()) {
            getActivity().moveTaskToBack(true);
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (BTController.getInstance().isSyncing()) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void dismissTipDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        LogUtils.d(TAG, "ContactsFragment >> onDestroyViewLazy");
        BTController.getInstance().unRegisterPbapCallback(hashCode());
        mHandler.removeAll();
        stopProgressDialog();
        dismissTipDialog();
        super.onDestroyViewLazy();
    }

    class NumberAdapter extends BaseAdapter {
        private List<ContactNumber> mDataList = new ArrayList<>();

        public void setDataList(List<ContactNumber> dataList) {
            mDataList.clear();
            if (dataList != null && dataList.size() > 0) {
                mDataList.addAll(dataList);
            }
            notifyDataSetChanged();
            LogUtils.d(TAG, "ContactsFragment >> NumberAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(ContactNumber data) {
            if (data != null) {
                mDataList.add(data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "ContactsFragment >> NumberAdapter >> addData >> size:" + mDataList.size());
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
                viewHolder.tvNumberType = (TextView) convertView.findViewById(R.id.tv_number_type);
                viewHolder.tvMatchNumber = (TextView) convertView.findViewById(R.id.tv_match_number);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ContactNumber contactNumber = (ContactNumber) getItem(position);
            //LogUtils.d(TAG, "ContactsFragment >> NumberAdapter >> getView >> mContactNumber:" + contactNumber.toString());

            viewHolder.tvNumberType.setText("");
            viewHolder.tvMatchNumber.setText("");
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
            viewHolder.tvNumberType.setText(numberType);
            viewHolder.tvMatchNumber.setText(DBBtUtil.numberFormat(DBBtUtil.handleText(contactNumber.getNumber(), 15)));
            // LogUtils.d(TAG, "ContactsFragment >> NumberAdapter >> getView >> numberType:" + numbertype + ",number:" + contactNumber.getNumber());

            return convertView;
        }

        private class ViewHolder {
            private TextView tvNumberType;
            private TextView tvMatchNumber;
        }
    }

    class ContactAdapter extends BaseAdapter {
        private List<Contact> mDataList = new ArrayList<>();
        private String firstChar;
        private String recordChar;

        public void setDataList(List<Contact> dataList) {
            mDataList.clear();
            if (dataList != null && dataList.size() != 0) {
                mDataList.addAll(dataList);
            }
            notifyDataSetChanged();
            LogUtils.d(TAG, "ContactsFragment >> ContactAdapter >> setDataList >> size:" + mDataList.size());
        }

        public void addData(Contact data) {
            if (data != null) {
                mDataList.add(data);
                notifyDataSetChanged();
                LogUtils.d(TAG, "ContactsFragment >> ContactAdapter >> addData >> size:" + mDataList.size());
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
                convertView = View.inflate(getApplicationContext(), R.layout.bt_contact_item_layout, null);

                viewHolder = new ViewHolder();
                viewHolder.contactLayout = (ConstraintLayout) convertView.findViewById(R.id.cl_bt_contact_item);
                viewHolder.tvInitials = (TextView) convertView.findViewById(R.id.tv_initials);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.iv_contact_name);
                viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_contact_number);
                viewHolder.viewDividerOne = convertView.findViewById(R.id.view_one);
                viewHolder.viewDividerTwo = convertView.findViewById(R.id.view_two);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = (Contact) getItem(position);
            viewHolder.viewDividerOne.setVisibility(View.GONE);
            viewHolder.viewDividerTwo.setVisibility(View.GONE);

            recordChar = null;

            firstChar = contact.getPinyin().substring(0, 1).toUpperCase();
            if (position != 0) {
                Contact oldContact = (Contact) getItem(position - 1);
                recordChar = oldContact.getPinyin().substring(0, 1).toUpperCase();
            }

            if (!firstChar.equals(recordChar)) {
                viewHolder.tvInitials.setVisibility(View.VISIBLE);
                viewHolder.tvInitials.setText(firstChar);
                viewHolder.viewDividerOne.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvInitials.setVisibility(View.INVISIBLE);
                viewHolder.viewDividerTwo.setVisibility(View.VISIBLE);
            }

            if (position == 0) {
                viewHolder.viewDividerOne.setVisibility(View.GONE);
            }

            if (position == mDataList.size()) {
                viewHolder.viewDividerOne.setVisibility(View.VISIBLE);
            }

            viewHolder.tvName.setText("");
            viewHolder.tvNumber.setText("");

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
                    viewHolder.tvNumber.setText(DBBtUtil.numberFormat(DBBtUtil.handleText(number, 15)));
                    if (TextUtil.isEmpty(contact.getName())) {
                        viewHolder.tvName.setText(DBBtUtil.handleText(number, 24));
                    }
                    break;
                }
            }

            if (!TextUtil.isEmpty(contact.getName())) {
                viewHolder.tvName.setText(DBBtUtil.handleText(contact.getName(), 24));
            }
            //LogUtils.d(TAG, "ContactsFragment >> ContactAdapter >> getView >> contact:" + contact.toString());

            viewHolder.contactLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mList != null && mList.size() != 0) {
                        if (mList.size() > 1) {
                            createContactDialog(contact.getName(), mList);
                        } else {
                            String number = mList.get(0).getNumber();
                            if (!TextUtil.isEmpty(number)) {
                                BTController.getInstance().dial(number);
                            }
                        }
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            private TextView tvInitials;
            private TextView tvName;
            private TextView tvNumber;
            private View viewDividerOne;
            private View viewDividerTwo;
            private ConstraintLayout contactLayout;
        }
    }
}
