package com.jsbd.btphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.jsbd.bluetooth.utils.LogUtils;
import com.jsbd.btphone.config.Config;

/**
 * Created by wills on 2017/12/18.
 */

public class MediaButtonReceiver extends BroadcastReceiver {

    private static String TAG = MediaButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获得Action
        String action = intent.getAction();
        // 获得KeyEvent对象
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        LogUtils.i(TAG, "MediaButtonReceiver >> onReceive >> action:" + action);

        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            // 获得按键字节码
            int keyCode = keyEvent.getKeyCode();
            // 按下 / 松开 按钮
            int keyAction = keyEvent.getAction();
            LogUtils.i(TAG, "MediaButtonReceiver >> onReceive >> keyAction:" + keyAction + ",keyCode:" + keyCode);

            if (keyAction == KeyEvent.ACTION_UP) {
                Intent actionIntent;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        LogUtils.d(TAG, "MediaButtonReceiver >> KEYCODE_MEDIA_PLAY");
                        actionIntent = new Intent(Config.ACTION_BRROADCAST_MEDIA_PLAY);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        LogUtils.d(TAG, "MediaButtonReceiver >> KEYCODE_MEDIA_PAUSE");
                        actionIntent = new Intent(Config.ACTION_BRROADCAST_MEDIA_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        LogUtils.d(TAG, "MediaButtonReceiver >> KEYCODE_MEDIA_NEXT");
                        actionIntent = new Intent(Config.ACTION_BRROADCAST_MEDIA_NEXT);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        LogUtils.d(TAG, "MediaButtonReceiver >> KEYCODE_MEDIA_PREVIOUS");
                        actionIntent = new Intent(Config.ACTION_BRROADCAST_MEDIA_PREVIOUS);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        LogUtils.d(TAG, "MediaButtonReceiver >> KEYCODE_MEDIA_PLAY_PAUSE");
                        actionIntent = new Intent(Config.ACTION_BRROADCAST_MEDIA_PLAY_PAUSE);
                        break;
                    default:
                        return;
                }

                context.sendBroadcast(actionIntent);
            }
        }
    }
}
