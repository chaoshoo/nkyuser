package com.ningkangyuan.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by xuchun on 2016/8/19.
 */
public class ScreenUtil {

    /**
     * dip转px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context,float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5f);
    }

    /**
     * px转dip
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dix(Context context,float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     * @param context
     * @return
     */
    public static int getWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     * @param context
     * @return
     */
    public static int getHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }


    public static String getDeviceId(Context context) {
        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return TelephonyMgr.getDeviceId();
    }
}
