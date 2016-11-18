package com.ningkangyuan.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doctor;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/26.
 */
public class QuestionSaveActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "QuestionSaveActivity";

    private ImageView mProtraitIV;
    private TextView mNameTV,mJobTV,mHistoryTV,mIntroTV;
    private EditText mTitleET,mContentET;

    private Doctor mDoctor;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.question_save, null));

        mProtraitIV = (ImageView) findViewById(R.id.doctor_details_protrait);
        mNameTV = (TextView) findViewById(R.id.doctor_details_name);
        mJobTV = (TextView) findViewById(R.id.doctor_details_job);
        mHistoryTV = (TextView) findViewById(R.id.doctor_details_edu);
        mIntroTV = (TextView) findViewById(R.id.doctor_details_intro);
        mTitleET = (EditText) findViewById(R.id.question_save_title);
        mContentET = (EditText) findViewById(R.id.question_save_content);

        findViewById(R.id.question_save_confirm).setOnClickListener(this);
        findViewById(R.id.question_save_cancel).setOnClickListener(this);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.question_save_confirm:
                //确定
                submit();
                break;
            case R.id.question_save_cancel:
                //取消
                finish();
                break;
        }
    }

    public void submit() {
        String title = mTitleET.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            ToastUtil.show(this,"请输入标题");
            return;
        }
        final String content = mContentET.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(this,"请填写内容");
            return;
        }
        showProgressDialog("正在提交..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("questionsave",
                new String[]{"vip_code", "doctor_code", "title", "content", "attachement"},
                new Object[]{mVip.getVip_code(), mDoctor.getCode(), title, content, ""}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(QuestionSaveActivity.this, e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String code = JsonUtil.getObjectByKey("code", result);
                final String message = JsonUtil.getObjectByKey("message", result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(QuestionSaveActivity.this, message);
                        if ("1".equals(code)) {
                            finish();
                        }
                    }
                });
            }
        }));
    }
}
