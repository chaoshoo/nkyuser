package com.ningkangyuan.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuchun on 2016/8/23.
 */
public class JsonUtil {

    public static final Gson mGson = new Gson();

    /**
     * 根据key获取value
     * 意外情况返回null
     * @param key
     * @param jsonStr
     * @return
     */
    public static String getObjectByKey(String key,String jsonStr) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(key)) {
                return jsonObject.get(key).toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}