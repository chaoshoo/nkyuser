package com.ningkangyuan.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doctor;
import com.ningkangyuan.bean.RemoteHistory;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/30.
 */
public class RemoteDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RemoteDetailsActivity";

    private ImageView mProtraitIV;
    private TextView mNameTV,mJobTV,mHistoryTV,mIntroTV;

    private TextView mOrderTimeTV,mIsZDTV,mBeginTimeTV,mIsDealTV,mEndTimeTV,mRemarkTV;
    private RatingBar mEvaluateRB;

    private RemoteHistory mRemote;
    private Button mCancelBtn,mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mNameTV;
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("检查卡号：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.remote_details, null));

        mProtraitIV = (ImageView) findViewById(R.id.doctor_details_protrait);
        mNameTV = (TextView) findViewById(R.id.doctor_details_name);
        mJobTV = (TextView) findViewById(R.id.doctor_details_job);
        mHistoryTV = (TextView) findViewById(R.id.doctor_details_edu);
        mIntroTV = (TextView) findViewById(R.id.doctor_details_intro);

        mOrderTimeTV = (TextView) findViewById(R.id.remote_details_order_time);
        mIsZDTV = (TextView) findViewById(R.id.remote_details_iszd);
        mBeginTimeTV = (TextView) findViewById(R.id.remote_details_begin_time);
        mIsDealTV = (TextView) findViewById(R.id.remote_details_isDeal);
        mEndTimeTV = (TextView) findViewById(R.id.remote_details_end_time);
        mRemarkTV = (TextView) findViewById(R.id.remote_details_remark);
        mEvaluateRB = (RatingBar) findViewById(R.id.remote_details_evaluate);

        mCancelBtn = (Button) findViewById(R.id.remote_details_cancel);
        mBackBtn = (Button) findViewById(R.id.remote_details_back);

        mCancelBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);

        mRemote = (RemoteHistory) getIntent().getSerializableExtra("remote");
        getDoctorInfo();
        mOrderTimeTV.setText(mRemote.getOrder_time());
        mRemarkTV.setText(mRemote.getRemark());
        if ("1".equals(mRemote.getIszd())) {
            mIsZDTV.setText("是");
            mBeginTimeTV.setText(mRemote.getZd_begin_time());
            if ("1".equals(mRemote.getIsdeal())) {
                mIsDealTV.setText("是");
                mEndTimeTV.setText(mRemote.getZd_end_Time());
                //评价
//                mEvaluateRB.setRating();
                //此时不行取消此次预约
                mCancelBtn.setVisibility(View.GONE);
            } else {
                findViewById(R.id.remote_details_isDeal_layout).setVisibility(View.GONE);
                findViewById(R.id.remote_details_end_time_layout).setVisibility(View.GONE);
                findViewById(R.id.remote_details_evaluate_layout).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.remote_details_iszd_layout).setVisibility(View.GONE);
            findViewById(R.id.remote_details_begin_time_layout).setVisibility(View.GONE);
            findViewById(R.id.remote_details_isDeal_layout).setVisibility(View.GONE);
            findViewById(R.id.remote_details_end_time_layout).setVisibility(View.GONE);
            findViewById(R.id.remote_details_evaluate_layout).setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.remote_details_cancel:
                if ("1".equals(mRemote.getIsdeal())) {
                    ToastUtil.show(this,"此远程咨询已处理过，不能被取消");
                    return;
                }
                cancelSubscribe();
                break;
            case R.id.remote_details_back:
                //返回
                finish();
                break;
        }
    }

    private void initDoctorInfo(final Doctor doctor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                if (doctor == null) {
                    ToastUtil.show(RemoteDetailsActivity.this, "医生信息获取失败");
                    return;
                }
                mNameTV.setText(doctor.getName());
                mJobTV.setText(doctor.getTitle());
                mHistoryTV.setText(doctor.getEdu());
                mIntroTV.setText(doctor.getInfo());
            }
        });

    }

    private void getDoctorInfo() {
        showProgressDialog("正在查询医生信息..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("doctors",
                new String[]{"code","office_code","hospital_code"},
                new Object[]{mRemote.getDoctor_code(),"",mRemote.getHospital_code()}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RemoteDetailsActivity.this, "onFailure：" + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                String code = JsonUtil.getObjectByKey("code", result);
                if ("1".equals(code)) {
                    String doctors = JsonUtil.getObjectByKey("doctors",result);
                    List<Doctor> tempList = JsonUtil.mGson.fromJson(doctors,new TypeToken<List<Doctor>>() {}.getType());
                    if (!tempList.isEmpty()) {
                        initDoctorInfo(tempList.get(0));
                    } else {
                        initDoctorInfo(null);
                    }
                } else {
                    initDoctorInfo(null);
                }
            }
        }));
    }

    private void cancelSubscribe() {
        showProgressDialog("正在取消..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("remotecancel",
                new String[]{"remote_code"},
                new Object[]{mRemote.getCode()}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RemoteDetailsActivity.this, "onFailure：" + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String code = JsonUtil.getObjectByKey("code", result);
                final String message = JsonUtil.getObjectByKey("message",result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RemoteDetailsActivity.this,message);
                        if ("1".equals(code)) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
            }
        }));
    }
}
