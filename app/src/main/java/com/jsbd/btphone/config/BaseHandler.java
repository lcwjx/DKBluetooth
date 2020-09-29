package com.jsbd.btphone.config;

import android.os.Handler;

/**
 * Created by chenwei on 16/3/29.
 */
public class BaseHandler extends Handler {

    //清理Handler的message
    public void removeAll() {
        super.removeCallbacksAndMessages(null);
    }
}
