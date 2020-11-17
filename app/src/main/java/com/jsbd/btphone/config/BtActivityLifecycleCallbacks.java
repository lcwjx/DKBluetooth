package com.jsbd.btphone.config;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jsbd.bluetooth.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenwei on 15/9/23.
 */
public class BtActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static String TAG = "BtActivityLifecycleCallbacks";

    private List<Activity> activities = new ArrayList<>();

    private int inActive = 0;


    private Handler mHandler = new InnerHandler(this);

    static class InnerHandler extends WeakHandler<BtActivityLifecycleCallbacks> {

        public InnerHandler(BtActivityLifecycleCallbacks who) {
            super(who);
        }

        @Override
        public void onHandleMessage(BtActivityLifecycleCallbacks self, Message msg) {
            switch (msg.what) {
                case 0x01:
                    if (self.inActive == 0) {

                    }
                    break;
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (!activities.contains(activity))
            activities.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        inActive++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mHandler.removeMessages(0x01);
        mHandler.sendEmptyMessageDelayed(0x01, 500);
        inActive--;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activities.contains(activity))
            activities.remove(activity);

        LogUtils.i(TAG, "BtActivityLifecycleCallbacks >> onActivityDestroyed >> name:" + activity.getClass().getSimpleName() + " Destroyed");
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void release() {
        activities.clear();
    }
}
