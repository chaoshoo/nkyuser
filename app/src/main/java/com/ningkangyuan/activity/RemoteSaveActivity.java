package com.ningkangyuan.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doctor;
import com.ningkangyuan.datepicker.MyPicker;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 诊断预约
 * Created by xuchun on 2016/8/24.
 */
public class RemoteSaveActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RemoteSaveActivity";

    private ImageView mProtraitIV;
    private TextView mNameTV,mJobTV,mHistoryTV,mIntroTV;
//    private TextView mDateTV,mTimeTV;
    private EditText mDateET,mTimeET;
    private EditText mRemarkET;

//    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
//    private MyPicker mDatePicker;
//    private MyPicker mTimePicker;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");


    private Doctor mDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mDateET;
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("检查卡号：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.remote_save, null));

        mProtraitIV = (ImageView) findViewById(R.id.doctor_details_protrait);
        mNameTV = (TextView) findViewById(R.id.doctor_details_name);
        mJobTV = (TextView) findViewById(R.id.doctor_details_job);
        mHistoryTV = (TextView) findViewById(R.id.doctor_details_edu);
        mIntroTV = (TextView) findViewById(R.id.doctor_details_intro);

//        mDateTV = (TextView) findViewById(R.id.remote_save_date);
//        mTimeTV = (TextView) findViewById(R.id.remote_save_time);
        mDateET = (EditText) findViewById(R.id.remote_save_date);
        mTimeET = (EditText) findViewById(R.id.remote_save_time);
        mRemarkET = (EditText) findViewById(R.id.remote_save_remark);

//        findViewById(R.id.remote_save_date_layout).setOnClickListener(this);
//        findViewById(R.id.remote_save_time_layout).setOnClickListener(this);
        findViewById(R.id.remote_save_confirm).setOnClickListener(this);
        findViewById(R.id.remote_save_cancel).setOnClickListener(this);

        mDoctor = (Doctor) getIntent().getSerializableExtra("doctor");
        if (mDoctor != null) {

//            ImageLoaderHelper.getInstance().loader("",mProtraitIV,ImageLoaderHelper.makeImageOptions());

            mNameTV.setText(mDoctor.getName());
            mJobTV.setText(mDoctor.getTitle());
            mHistoryTV.setText(mDoctor.getEdu());
            mIntroTV.setText(mDoctor.getInfo());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.remote_save_date:
//                DateDialog.getInstance().show(this, onDatePickedListener);
//                break;
            case R.id.remote_save_confirm:
                //确定
                submit();
                break;
            case R.id.remote_save_cancel:
                //取消
                finish();
                break;
            case R.id.remote_save_date_layout:
//                showDatePickerDialog();
                break;
            case R.id.remote_save_time_layout:
//                showTimePickerDialog();
                break;
        }
    }

    //选择日期
//    private void showDatePickerDialog() {
//        if (mDatePicker == null) {
//            mDatePicker = new MyPicker(this);
//            //年
//            List<String> years = new ArrayList<String>();
//            for(int i = 2016; i < 2050;i ++) {
//                years.add(i + "");
//            }
//            mDatePicker.setData(years,1);
//            //月
//            List<String> months = new ArrayList<String>();
//            for(int i = 1; i < 13;i ++) {
//                String month = "" + i;
//                if (i < 10) {
//                    month = "0" + i;
//                }
//                months.add(month);
//            }
//            mDatePicker.setData(months,2);
//            //日
//            List<String> days = new ArrayList<String>();
//            for(int i = 1; i < 32;i ++) {
//                String day = "" + i;
//                if (i < 10) {
//                    day = "0" + i;
//                }
//                days.add(day);
//            }
//            mDatePicker.setData(days,3);
//
//            mDatePicker.setMiddleText(0, 1);
//            mDatePicker.setMiddleText(0,2);
//            mDatePicker.setMiddleText(0,3);
//            mDatePicker.setPickerTitle("请选择日期");
//            mDatePicker.setPrepare();
//
//            mDatePicker.setSelectedFinishListener(new MyPicker.SelectedFinishListener() {
//                @Override
//                public void onFinish() {
//                    String year = String.valueOf(mDatePicker.getText(1));
//                    String month = String.valueOf(mDatePicker.getText(2));
//                    String day = String.valueOf(mDatePicker.getText(3));
//                    mDateTV.setText(year + "-" + month + "-" + day);
//                    mDatePicker.dismiss();
//                }
//            });
//        }
//        mDatePicker.showAtLocation(mDateTV, Gravity.CENTER,0,0);
//    }

    //选择时间
//    private void showTimePickerDialog() {
//        if (mTimePicker == null) {
//            mTimePicker = new MyPicker(this);
//            //AM PM
//            List<String> times = new ArrayList<String>();
//            times.add("上午");
//            times.add("下午");
//            mTimePicker.setData(times,1);
//            //小时
//            List<String> hours = new ArrayList<String>();
//            for(int i = 1; i < 13;i ++) {
//                String hour = "" + i;
//                if (i < 10) {
//                    hour = "0" + i;
//                }
//                hours.add(hour);
//            }
//            mTimePicker.setData(hours,2);
//            //分钟
//            List<String> minutes = new ArrayList<String>();
//            for(int i = 0; i < 60;i ++) {
//                String minute = "" + i;
//                if (i < 10) {
//                    minute = "0" + i;
//                }
//                minutes.add(minute);
//            }
//            mTimePicker.setData(minutes,3);
//
//
//            mTimePicker.setMiddleText(0, 1);
//            mTimePicker.setMiddleText(9, 2);
//            mTimePicker.setMiddleText(0,3);
//            mTimePicker.setPickerTitle("请选择时间");
//            mTimePicker.setPrepare();
//
//            mTimePicker.setSelectedFinishListener(new MyPicker.SelectedFinishListener() {
//                @Override
//                public void onFinish() {
//                    String time = String.valueOf(mTimePicker.getText(1));
//                    String hour = String.valueOf(mTimePicker.getText(2));
//                    String minute = String.valueOf(mTimePicker.getText(3));
//                    mTimeTV.setText(hour + ":" + minute + ":00 " + time);
//                    mTimePicker.dismiss();
//                }
//            });
//        }
//        mTimePicker.showAtLocation(mDateTV, Gravity.CENTER, 0, 0);
//    }

    public void submit() {
        String date = mDateET.getText().toString();
        if (TextUtils.isEmpty(date)) {
            ToastUtil.show(this,"请输入日期");
            return;
        }
        String time = mTimeET.getText().toString();
        if (TextUtils.isEmpty(time)) {
            ToastUtil.show(this,"请输入时间");
            return;
        }
        String orderTime = date + " " + time;
//        try {
//            orderTime = mSimpleDateFormat.format(new SimpleDateFormat("yyyy-MM-hh hh:mm:ss a").parseObject(date + " " + time));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        final String remark = mRemarkET.getText().toString().trim();
        showProgressDialog("正在提交..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("remotesave",
                new String[]{"vip_code","doctor_code","hospital_code","order_time","remark"},
                new Object[]{mVip.getVip_code(),mDoctor.getCode(),mDoctor.getHospital_code(),
                        orderTime,remark}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RemoteSaveActivity.this,e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,"onResponse：" + result);
                final String code = JsonUtil.getObjectByKey("code",result);
                final String message = JsonUtil.getObjectByKey("message",result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RemoteSaveActivity.this,message);
                        if ("1".equals(code)) {
                            finish();
                        }
                    }
                });
            }
        }));
    }
}
