package com.jsbd.btphone.config;

import android.support.annotation.IntDef;

/**
 * Created by wills on 2018/11/20.
 */

public class Config {
    public static final String ACTION_EXIT_APP = "com.jsbd.btphone.Action.exit_app";

    //通话悬浮窗Service的Action
    public static final String ACTION_SERVICE_FLOAT_WINDOW = "com.jsbd.btphone.CommunicationService.Action";

    //通话悬浮窗广播的Action，这个广播控制悬浮窗的显示/隐藏/销毁
    public static final String ACTION_BRROADCAST_FLOAT_WINDOW = "com.jsbd.btphone.CommunicationService.Action.FloatWindow";
    public static final String EXTRA_DATA_FLOAT_WINDOW = "floatWindowType";
    public static final String EXTRA_DATA_DELAY_TIME = "delayTime";

    //MediaButton接收到按键动作转发到下面的action里处理
    public static final String ACTION_BRROADCAST_MEDIA_PLAY = "com.jsbd.btphone.MediaButtonReceiver.Action.Play";
    public static final String ACTION_BRROADCAST_MEDIA_PAUSE = "com.jsbd.btphone.MediaButtonReceiver.Action.Pause";
    public static final String ACTION_BRROADCAST_MEDIA_PLAY_PAUSE = "com.jsbd.btphone.MediaButtonReceiver.Action.PlayPause";
    public static final String ACTION_BRROADCAST_MEDIA_NEXT = "com.jsbd.btphone.MediaButtonReceiver.Action.Next";
    public static final String ACTION_BRROADCAST_MEDIA_PREVIOUS = "com.jsbd.btphone.MediaButtonReceiver.Action.Previous";

    public static final String ACTION_BRROADCAST_JSBD_VR_APP = "com.jsbd.vr.app.action";  /*语音*/

    public static final int HIDE_FLOAT_WINDOW = 1;
    public static final int SHOW_FLOAT_WINDOW = 2;
    public static final int DESTROY_FLOAT_WINDOW = 3;

    @IntDef({HIDE_FLOAT_WINDOW, SHOW_FLOAT_WINDOW, DESTROY_FLOAT_WINDOW})
    public @interface FloatWindowType {
    }

    public static final int PAGE_IDLE = -1;
    public static final int PAGE_SETTING = 0;
    public static final int PAGE_DIAL = 1;
    public static final int PAGE_CALLLOG = 2;
    public static final int PAGE_CONTACT = 3;

    @IntDef({PAGE_IDLE, PAGE_SETTING, PAGE_DIAL, PAGE_CALLLOG, PAGE_CONTACT})
    public @interface PageType {
    }

    //扫描设备超时时长
    public static final int DISCOVERY_DURATION = 30; //单位秒
}
