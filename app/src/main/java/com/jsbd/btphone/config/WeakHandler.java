package com.jsbd.btphone.config;

import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by chenwei on 16/3/27.
 */
public abstract class WeakHandler<T> extends BaseHandler {


    private WeakReference<T> weakReference;

    public WeakHandler(T t) {
        super();
        weakReference = new WeakReference<>(t);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (weakReference != null && weakReference.get() != null) {
            onHandleMessage(weakReference.get(), msg);
        }
    }


    public abstract void onHandleMessage(T self, Message msg);

    //清理Handler的message
    public void removeAll() {
        super.removeAll();
        weakReference.clear();
        weakReference = null;
    }
}
