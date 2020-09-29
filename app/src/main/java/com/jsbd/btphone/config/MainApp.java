package com.jsbd.btphone.config;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.StrictMode;

import com.jsbd.app.BDApplication;
import com.jsbd.support.bluetooth.BTController;
import com.jsbd.support.bluetooth.utils.LogUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.List;

/**
 * Created by QY on 2018/9/10.
 */

public class MainApp extends BDApplication {
    private static final String TAG = "MainApp";
    private BtActivityLifecycleCallbacks lifecycleCallbacks;
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "MainApp >> onCreate");
        super.onCreate();
        BTController.getInstance().init(this);
       lifecycleCallbacks = new BtActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(lifecycleCallbacks);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int heapSize = manager.getMemoryClass();
        int maxHeapSize = manager.getLargeMemoryClass();
        LogUtils.d(TAG, "MainApp >> onCreate >> heapSize:" + heapSize + ",maxHeapSize:" + maxHeapSize);

        refWatcher = initLeakCanary();

        //initStrictMode();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.d(TAG, "MainApp >> onTerminate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtils.d(TAG, "MainApp >> onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtils.d(TAG, "MainApp >> onTrimMemory >> level:" + level);
    }

    private RefWatcher initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }

        return LeakCanary.install(this);
    }

    /**
     * 销毁所有activity
     */
    public void exitApp() {
        List<Activity> activityList = lifecycleCallbacks.getActivities();
        for (int i = 0; i < activityList.size(); i++)
            activityList.get(i).finish();
        lifecycleCallbacks.release();
    }

    /**
     * 销毁所有activity 除了exist
     *
     * @param exist
     */
    public void exitApp(Activity exist) {
        List<Activity> activityList = lifecycleCallbacks.getActivities();
        for (int i = 0; i < activityList.size(); i++) {
            if (exist != activityList.get(i))
                activityList.get(i).finish();
        }
    }

    private void initStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()//监测所有内容
                .penaltyLog()//违规对log日志
                .penaltyDeath()//违规Crash
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()//监测所以内容
                .penaltyLog()//违规对log日志
                .penaltyDeath()//违规Crash
                .build());

    }

    public static RefWatcher getRefWatcher() {
        return ((MainApp) getAppContext()).refWatcher;
    }

}
