package com.ningkangyuan.push;

import android.content.Context;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.ningkangyuan.utils.LogUtil;

import java.util.List;

/**
 * 百度推送接收器
 * Created by xuchun on 2016/8/15.
 */
public class BDPushReceiver extends PushMessageReceiver {

    public static String android_tv_channel_id;

    public static OnPushListener mOnPushListener;

    private static final String TAG = "PushReceiver";

    @Override
    public void onBind(Context context, int errorCode, String appid, String userId, String channelId, String requestId) {
        LogUtil.d(TAG,"onBind：errorCode：" + errorCode);
        LogUtil.d(TAG,"onBind：appid：" + appid + "\nuserId：" + userId + "\nchannelId：" + channelId + "\nrequestId：" + requestId);
        android_tv_channel_id = channelId;
    }

    @Override
    public void onUnbind(Context context, int i, String s) {
        LogUtil.d(TAG,"onUnbind：s：" + s);
    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {

    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {
        //接收消息
        LogUtil.d(TAG,"onMessage：message：" + message + "\ncustomContentString：" + customContentString);
        //将此消息存起来，然后在消息中心展示
        LogUtil.d(TAG,"mOnPushListener：" + mOnPushListener);
        if (mOnPushListener != null) {
            mOnPushListener.inMessage(message);
        }
    }

    @Override
    public void onNotificationClicked(Context context, String s, String s1, String s2) {

    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {

    }


}
