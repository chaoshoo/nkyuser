package com.ningkangyuan;

import com.ningkangyuan.kpi.Measure;
import com.ningkangyuan.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuchun on 2016/9/10.
 */
public class Constant {

    private static final String TAG = "Constant";

    public static final int PAGE_SIZE = 15;

    public static final int PAGE_SIZE_10 = 10;

    public static final int PAGE_SIZE_8 = 8;

    public static List<JSONObject> NORM;

    public static Map<String,String> REFERENCE;

    static {
        LogUtil.d(TAG,"static ...");
        NORM = new ArrayList<JSONObject>();
        try {
            JSONObject xueya = new JSONObject();
            xueya.put("inspect_code","C01");
            xueya.put("inspect_name","blood pressure");
            NORM.add(xueya);

            JSONObject xuetang = new JSONObject();
            xuetang.put("inspect_code","C02");
            xuetang.put("inspect_name", "blood sugar");
            NORM.add(xuetang);

            JSONObject bmi = new JSONObject();
            bmi.put("inspect_code","C03");
            bmi.put("inspect_name", "BMI");
            NORM.add(bmi);

            JSONObject tiwen = new JSONObject();
            tiwen.put("inspect_code","C04");
            tiwen.put("inspect_name","temperature");
            NORM.add(tiwen);

            JSONObject xueyang = new JSONObject();
            xueyang.put("inspect_code","C05");
            xueyang.put("inspect_name","Oxygen");
            NORM.add(xueyang);

            JSONObject niao = new JSONObject();
            niao.put("inspect_code","C06");
            niao.put("inspect_name","Urine routine");
            NORM.add(niao);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        REFERENCE = new HashMap<String,String>();
        REFERENCE.put(Measure.XueYa.INSPECT_CODE,"");
    }

}