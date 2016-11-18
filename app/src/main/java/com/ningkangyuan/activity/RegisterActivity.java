package com.ningkangyuan.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.ningkangyuan.R;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.ningkangyuan.utils.VerifyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/19.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText mIdentityET,mPhoneET,mAuthcodeET,mPasswordET;
    private Button mAuthcodeBTN,mSubmitBTN;

    private int mCount = 60;
    private Timer mTimer = new Timer();

    private String mIdCard,mPhone,mPassword,mAuthPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isLoadVip = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mIdentityET;
    }

    @Override
    protected void init() {

        findViewById(R.id.universal_checkcard_num).setVisibility(View.GONE);

        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.register, null));

        mIdentityET = (EditText) findViewById(R.id.register_identity);
        mPhoneET = (EditText) findViewById(R.id.register_phone);
        mAuthcodeET = (EditText) findViewById(R.id.register_authcode);
        mPasswordET = (EditText) findViewById(R.id.register_password);

        mAuthcodeBTN = (Button) findViewById(R.id.register_getcode);
        mSubmitBTN = (Button) findViewById(R.id.register_submit);
        mAuthcodeBTN.setOnClickListener(this);
        mSubmitBTN.setOnClickListener(this);

        findViewById(R.id.register_back).setOnClickListener(this);

//        mIdentityET.setText("420521199210112237");
//        mPhoneET.setText("18907186852");
//        mPasswordET.setText("xc123456");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_getcode:
                //获取验证码
                mAuthPhone = mPhoneET.getText().toString().trim();
                if (TextUtils.isEmpty(mAuthPhone)) {
                    ToastUtil.show(this,"请输入手机号码");
                    return;
                }
                if (!VerifyUtil.isMobile(mAuthPhone)) {
                    ToastUtil.show(this,"请输入正确的手机号码");
                    return;
                }
                getAuthCode(mAuthPhone);
                break;
            case R.id.register_submit:
                //提交
                submit();
                break;
            case R.id.register_back:
                finish();
                break;
        }
    }

    public void getAuthCode(String tel) {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        //获取验证码
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mCount --;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAuthcodeBTN.setText("重新获取(" + mCount + ")");
                        if (mCount == 0) {
                            mTimer.cancel();
                            mTimer = null;

                            mCount = 60;
                            mAuthcodeBTN.setText("重新获取");
                            mAuthcodeBTN.setEnabled(true);
                        }
                    }
                });
            }
        };
        mAuthcodeBTN.setEnabled(false);

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("type","registMessage");
            jsonParams.put("tel",tel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpHelper.get(jsonParams, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed"))  {
                            msg = "无法连接服务器，请检查网络";
                        }
                        ToastUtil.show(RegisterActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,result);
                final String success = JsonUtil.getObjectByKey("success", result);
                final String message = JsonUtil.getObjectByKey("message", result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show(RegisterActivity.this,message);
                        if ("true".equals(success)) {
                            mTimer.schedule(timerTask, 0, 1000);
                        } else {
                            mAuthcodeBTN.setEnabled(true);
                        }
                    }
                });

            }
        });
    }

    private void submit() {
        mIdCard = mIdentityET.getText().toString().trim();
        mPhone = mPhoneET.getText().toString().trim();
        String authCode = mAuthcodeET.getText().toString().trim();
        mPassword = mPasswordET.getText().toString().trim();

        if (TextUtils.isEmpty(mIdCard)) {
            ToastUtil.show(this,"请输入身份证");
            return;
        }
//        String tempStr = VerifyUtil.IDCardValidate(mIdCard);
//        if (!TextUtils.isEmpty(tempStr)) {
//            ToastUtil.show(this,tempStr);
//            return;
//        }

        if (TextUtils.isEmpty(mPhone)) {
            ToastUtil.show(this,"请输入手机号码");
            return;
        }
        if (!VerifyUtil.isMobile(mPhone)) {
            ToastUtil.show(this,"请输入正确的手机号码");
            return;
        }

        if (TextUtils.isEmpty(authCode)) {
            ToastUtil.show(this,"请输入验证码");
            return;
        }

        if (!mPhone.equals(mAuthPhone)) {
            ToastUtil.show(this,"请输入接收短信的手机号码");
            return;
        }

        if (TextUtils.isEmpty(mPassword)) {
            ToastUtil.show(this,"请输入密码");
            return;
        }

        JSONObject jssonParams = new JSONObject();
        try {
            jssonParams.put("","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog("正在提交..");
        JSONObject params = new JSONObject();
        try {
            params.put("type","checkmessage");
            params.put("tel",mPhone);
            params.put("code",authCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCallList.add(OkHttpHelper.get(params, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed"))  {
                            msg = "无法连接服务器，请检查网络";
                        }
                        ToastUtil.show(RegisterActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,result);
                final String success = JsonUtil.getObjectByKey("success", result);
                final String message = JsonUtil.getObjectByKey("message", result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ("true".equals(success)) {
                            register();
                            return;
                        }
                        dismissProgressDialog();
                        ToastUtil.show(RegisterActivity.this,message);
                    }
                });
            }
        }));

    }

    private void register() {
        JSONObject params = new JSONObject();
        try {
            params.put("type","useregist");
            params.put("papers_num",mIdCard);
            params.put("mobile",mPhone);
            params.put("login_password",mPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mCallList.add(OkHttpHelper.get(params, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RegisterActivity.this,e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,result);
                final String code = JsonUtil.getObjectByKey("code",result);
                final String message= JsonUtil.getObjectByKey("message",result);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(RegisterActivity.this,message);
                        if ("1".equals(code)) {
                            //注册成功
                            finish();
                        }
                    }
                });
            }
        }));
    }
}
