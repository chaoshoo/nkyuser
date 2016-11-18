package com.ningkangyuan.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doctor;
import com.ningkangyuan.bean.QuestionHistory;
import com.ningkangyuan.bean.QuestionLog;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/9/7.
 */
public class QuestionDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "QuestionSaveActivity";

    private ImageView mProtraitIV;
    private TextView mNameTV,mJobTV,mHistoryTV,mIntroTV;
    private EditText mAddET;

    private TextView mDate0TV,mContent0TV;
    private TextView mDate1TV,mContent1TV;

    private List<QuestionLog> mQuestionLogs = new ArrayList<QuestionLog>();
    private Doctor mDoctor;
    private QuestionHistory mQuestion;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.question_details, null));

        mProtraitIV = (ImageView) findViewById(R.id.doctor_details_protrait);
        mNameTV = (TextView) findViewById(R.id.doctor_details_name);
        mJobTV = (TextView) findViewById(R.id.doctor_details_job);
        mHistoryTV = (TextView) findViewById(R.id.doctor_details_edu);
        mIntroTV = (TextView) findViewById(R.id.doctor_details_intro);

        mDate0TV = (TextView) findViewById(R.id.question_details_date0);
        mContent0TV = (TextView) findViewById(R.id.question_details_content0);
        mDate1TV = (TextView) findViewById(R.id.question_details_date1);
        mContent1TV = (TextView) findViewById(R.id.question_details_content1);
        mAddET = (EditText) findViewById(R.id.question_details_add);

        findViewById(R.id.question_details_next).setOnClickListener(this);
        findViewById(R.id.question_details_up).setOnClickListener(this);
        findViewById(R.id.question_details_confirm).setOnClickListener(this);
        findViewById(R.id.question_details_back).setOnClickListener(this);

        mQuestion = (QuestionHistory) getIntent().getSerializableExtra("question");
        mDate0TV.setText("提交时间：" + mQuestion.getCreate_time());
        mContent0TV.setText("  " + mQuestion.getContent());
        getDoctorInfo();
        getQuestionLog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.question_details_back:
                finish();
                break;
            case R.id.question_details_confirm:
                //确定追加评论
                add();
                break;
            case R.id.question_details_next:
                //下一条留言
                showData("+");
                break;
            case R.id.question_details_up:
                //上一条留言
                showData("-");
                break;
        }
    }

    private void showDoctor() {
        if (mDoctor != null) {
//            ImageLoaderHelper.getInstance().loader("",mProtraitIV,ImageLoaderHelper.makeImageOptions());

            mNameTV.setText(mDoctor.getName());
            mJobTV.setText(mDoctor.getTitle());
            mHistoryTV.setText(mDoctor.getEdu());
            mIntroTV.setText(mDoctor.getInfo());
        }
    }

    private int count = 0;
    private void showData(String type) {
        if (mQuestionLogs.isEmpty()) {
            ToastUtil.show(this,"暂无消息");
            return;
        }
        if ("+".equals(type)) {
            if (count == (mQuestionLogs.size() - 1)) {
                ToastUtil.show(this,"已经是最后一条了");
                return;
            }
            count ++;
        } else if ("-".equals(type)) {
            if (count == 0) {
                ToastUtil.show(this,"已经是第一条了");
                return;
            }
            count --;
        }

        QuestionLog questionLog = mQuestionLogs.get(count);
        mDate1TV.setText("回复时间：" + questionLog.getCreate_time());
        mContent1TV.setText("  " + questionLog.getAnswer_content());

    }

    private void getDoctorInfo() {
        showProgressDialog("正在获取医生信息..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("doctors",
                new String[]{"code","name","pageIndex","pageSize","office_code","hospital_code"},
                new Object[]{mQuestion.getDoctor_code(),"","1","5","",""}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(QuestionDetailsActivity.this, "onFailure：" + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,"onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code",result))) {
                    String doctors = JsonUtil.getObjectByKey("doctors",result);
                    final List<Doctor> tempList = JsonUtil.mGson.fromJson(doctors,new TypeToken<List<Doctor>>() {}.getType());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            if (tempList.isEmpty()) {
                                ToastUtil.show(QuestionDetailsActivity.this, "暂无医生信息");
                                return;
                            }
                            mDoctor = tempList.get(0);
                            showDoctor();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            ToastUtil.show(QuestionDetailsActivity.this, "暂无医生信息");
                        }
                    });
                }
            }
        }));
    }

    private void getQuestionLog() {
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("questioninfo",
                        new String[]{"vip_questions_id"},
                        new Object[]{mQuestion.getId()}), new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                ToastUtil.show(QuestionDetailsActivity.this, "onFailure：" + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        LogUtil.d(TAG, "onResponse：" + result);
                        if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                            String questions = JsonUtil.getObjectByKey("questions", result);
                            List<QuestionLog> tempList = JsonUtil.mGson.fromJson(questions, new TypeToken<List<QuestionLog>>() {
                            }.getType());
                            mQuestionLogs.clear();
                            mQuestionLogs.addAll(tempList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    showData(null);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                }
                            });
                        }
                    }
                }));
    }

    private void add() {
        String addStr = mAddET.getText().toString().trim();
        if (TextUtils.isEmpty(addStr)) {
            ToastUtil.show(QuestionDetailsActivity.this, "请输入追加内容");
            return;
        }
        showProgressDialog("正在提交..");
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("questionlogsave",
                        new String[]{"answer_code","vip_questions_id","answer_content"},
                        new Object[]{mVip.getVip_code(),mQuestion.getId(),addStr}), new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                ToastUtil.show(QuestionDetailsActivity.this, "onFailure：" + e.getMessage());
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
                                ToastUtil.show(QuestionDetailsActivity.this, "" + message);
                                if ("1".equals(code)) {
                                    mAddET.setText("");
                                }
                            }
                        });
                    }
                }));
    }
}
