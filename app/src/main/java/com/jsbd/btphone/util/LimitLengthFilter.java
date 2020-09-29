package com.jsbd.btphone.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.jsbd.support.bluetooth.utils.LogUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EitText长度限制过滤器，英文/符号算半个字符，中文算1个字符。
 * Created by wills on 2018/12/13.
 */

public class LimitLengthFilter {
    private static final String TAG = "LimitLengthFilter";

    private static int maxCharEms;
    private static int currCharEms;

    private static final int LIMIT_COUNT = 12;

    public static void setLimitLength(EditText editText) {
        setLimitLength(editText, LIMIT_COUNT);
    }

    public static void setLimitLength(final EditText editText, int limit) {
        if (editText == null) return;

        maxCharEms = limit * 2;
        currCharEms = 0;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //LogUtils.d(TAG, "LimitInputFilter >> beforeTextChanged=" + charSequence.toString() + ",start=" + start + ",count=" + count + ",after:" + after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                LogUtils.d(TAG, "LimitInputFilter >> onTextChanged=" + charSequence.toString() + ",start=" + start + ",before=" + before + ",count:" + count + ",maxCharEms:" + maxCharEms);
                String string = charSequence.toString();
                if (getTotalCharEms(string) > maxCharEms) {
                    editText.setText(getSubChars(string));
                    editText.setSelection(editText.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //LogUtils.d(TAG, "LimitInputFilter >> afterTextChanged=" + editable.toString());
            }
        });
    }

    private static int getCharEms(CharSequence source) {
        String txt = source.toString();

        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(txt);
        if (m.matches()) {
            return 1;
        }
        p = Pattern.compile("[a-zA-Z]");
        m = p.matcher(txt);
        if (m.matches()) {
            return 1;
        }
        p = Pattern.compile("[\u4e00-\u9fa5]");
        m = p.matcher(txt);
        if (m.matches()) {
            return 2;
        }

        return 1;
    }

    private static int getTotalCharEms(CharSequence dest) {
        int totalNum = 0;
        for (int i = 0; i < dest.length(); i++) {
            CharSequence charSequence = dest.subSequence(i, 1 + i);
            int chNum = getCharEms(charSequence);
            totalNum += chNum;
        }
        LogUtils.d(TAG, "LimitInputFilter >> getTotalCharEms=" + totalNum);
        return totalNum;
    }

    private static String getSubChars(CharSequence dest) {
        StringBuilder result = new StringBuilder();
        int totalNum = 0;
        for (int i = 0; i < dest.length(); i++) {
            CharSequence charSequence = dest.subSequence(i, 1 + i);
            int chNum = getCharEms(charSequence);
            totalNum += chNum;
            if (totalNum <= maxCharEms) {
                result.append(charSequence);
            }
        }

        return result.toString();
    }
}
