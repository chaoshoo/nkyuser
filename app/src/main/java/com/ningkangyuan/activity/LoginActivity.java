package com.ningkangyuan.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.push.BDPushReceiver;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/15.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText mIdentityET,mPhoneET,mPasswordET;
    private String mIdentityStr,mPhoneStr,mPasswordStr;

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

        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.login, null));

        mIdentityET = (EditText) findViewById(R.id.login_identity);
//        mPhoneET = (EditText) findViewById(R.id.login_phone);
        mPasswordET = (EditText) findViewById(R.id.login_password);

        findViewById(R.id.login_submit).setOnClickListener(this);
        TextView registerBtn = (TextView) findViewById(R.id.login_register);
        registerBtn.setOnClickListener(this);
        registerBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        registerBtn.getPaint().setAntiAlias(true);

//        mIdentityET.setText("18907186852");
//        mIdentityET.setText("18986273972");
//        mPhoneET.setText("18907186852");
//        mPasswordET.setText("xc123456");
//        mPasswordET.setText("778844");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_submit:
                //登录
                login();
                break;
            case R.id.login_register:
                Intent intent = new Intent(this,RegisterActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void login() {
        if (checkParams()) {
            showProgressDialog("Logging in..");

            OkHttpHelper.get(OkHttpHelper.makeJsonParams("userlogin",
                    new String[]{"num","password","android_tv_channel_id"},
                    new Object[]{mIdentityStr,mPasswordStr, BDPushReceiver.android_tv_channel_id}), new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            String msg = e.getMessage();
                            if (msg.startsWith("Failed"))  {
                                msg = "Unable to connect to the server，Please check the network";
                            }
                            ToastUtil.show(LoginActivity.this, msg);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String result = response.body().string();
                    LogUtil.d(TAG, result);
                    final String code = JsonUtil.getObjectByKey("code", result);
                    final String message= JsonUtil.getObjectByKey("message", result);
                    final String vip_info= JsonUtil.getObjectByKey("vip_info", result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            ToastUtil.show(LoginActivity.this, message);
                            if ("1".equals(code)) {
                                //将登陆数据存起来
                                LoginActivity.this.getSharedPreferences("login",0).edit().putString("vip",vip_info).commit();

                                //保存当前登录的账号和密码
                                LoginActivity.this.getSharedPreferences("login",0).edit().putString("identityStr",mIdentityStr).commit();
                                LoginActivity.this.getSharedPreferences("login",0).edit().putString("passwordStr", mPasswordStr).commit();

                                //跳转至主页面
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            });
        }
    }

    //校验登录参数
    private boolean checkParams() {
        mIdentityStr = mIdentityET.getText().toString().trim();
        if (TextUtils.isEmpty(mIdentityStr)) {
            ToastUtil.show(this,"Please enter your ID or phone number.");
            return false;
        }
//        mPhoneStr = mPhoneET.getText().toString().trim();
//        if (TextUtils.isEmpty(mIdentityStr) && TextUtils.isEmpty(mPhoneStr)) {
//            ToastUtil.show(this,"身份证或手机号至少填一个");
//            return false;
//        }
//        if (!TextUtils.isEmpty(mIdentityStr)) {
//            String verifyResult = VerifyUtil.IDCardValidate(mIdentityStr);
//            if (!TextUtils.isEmpty(verifyResult)) {
//                ToastUtil.show(this, verifyResult);
//                return false;
//            }
//        } else {
//            mIdentityStr = "";
//        }
//        if (!TextUtils.isEmpty(mPhoneStr)) {
//            if (!VerifyUtil.isMobile(mPhoneStr)) {
//                ToastUtil.show(this, "请输入正确的手机号码");
//                return false;
//            }
//        } else {
//            mPhoneStr = "";
//        }
        mPasswordStr = mPasswordET.getText().toString().trim();
        if (TextUtils.isEmpty(mPasswordStr)) {
            mPasswordET.setHintTextColor(Color.RED);
            return false;
        }
        return true;
    }
}