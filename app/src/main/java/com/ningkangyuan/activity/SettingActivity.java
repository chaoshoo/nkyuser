package com.ningkangyuan.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.bridge.RecyclerViewBridge;
import com.open.androidtvwidget.view.MainUpView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 设置
 * Created by xuchun on 2016/8/29.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "SettingActivity";

    private MainUpView mMainUpView;
    private EffectNoDrawBridge mEffectNoDrawBridge;
//    private RecyclerViewBridge mRecyclerViewBridge;

    private Button mBackBtn,mUpdateBtn,mVersionBtn,mExitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("Check card number：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.setting, null));

        mMainUpView = (MainUpView) findViewById(R.id.setting_mainupview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        mEffectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        mEffectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.
//        mMainUpView.setEffectBridge(new RecyclerViewBridge());
//        mRecyclerViewBridge = (RecyclerViewBridge) mMainUpView.getEffectBridge();
//        mRecyclerViewBridge.setUpRectResource(R.drawable.video_cover_cursor);
//        float density = getResources().getDisplayMetrics().density;
//        RectF rectF = new RectF(35.0f * density,30.0f * density,35.0f * density,30.0f * density);
//        mRecyclerViewBridge.setDrawUpRectPadding(rectF);

        mBackBtn = (Button) findViewById(R.id.setting_version_back);
        mUpdateBtn = (Button) findViewById(R.id.setting_version_update);
        mVersionBtn = (Button) findViewById(R.id.setting_current_version);
        mExitBtn = (Button) findViewById(R.id.setting_exit);
        mBackBtn.setOnClickListener(this);
        mUpdateBtn.setOnClickListener(this);
        mVersionBtn.setOnClickListener(this);
        mExitBtn.setOnClickListener(this);

        mBackBtn.setOnFocusChangeListener(this);
        mUpdateBtn.setOnFocusChangeListener(this);
        mVersionBtn.setOnFocusChangeListener(this);
        mExitBtn.setOnFocusChangeListener(this);

        mUpdateBtn.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_version_update:
                //
//                ToastUtil.show(this,"正在火速处理");
                checkNewVersion();
                break;
            case R.id.setting_current_version:
                PackageManager packageManager = getPackageManager();
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
                    ToastUtil.show(this,"The current version is：" + packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.setting_exit:
                showExitDialog();
                break;
            case R.id.setting_version_back:
                finish();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mMainUpView.setFocusView(v,1,1f);
        } else {
            mMainUpView.setUnFocusView(v);
        }
    }

    private AlertDialog.Builder mExitDialog;
    private void showExitDialog() {
        if (mExitDialog == null) {
            mExitDialog = new AlertDialog.Builder(this);
            mExitDialog.setTitle("Prompt");
            mExitDialog.setMessage("Confirm to quit the current account？");
            mExitDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences("login",0).edit().putString("identityStr",null).commit();
                    getSharedPreferences("login",0).edit().putString("passwordStr",null).commit();
                    Intent intent = new Intent(SettingActivity.this,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            mExitDialog.setNegativeButton("no", null);
        }
        mExitDialog.show();
    }

    private Handler mUpdateApkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int progress = msg.arg1;
                    ToastUtil.show(SettingActivity.this,"Download progress:" + progress + "%");
                    break;
                case 2:
                    String apkPath = (String) msg.obj;
                    ToastUtil.show(SettingActivity.this,"Download completed，To install");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(apkPath)),"application/vnd.android.package-archive");
                    startActivity(intent);
                    break;
            }

        }
    };

    private Call mDownLoadCall;
    private void checkNewVersion() {
        mDownLoadCall = OkHttpHelper.get(OkHttpHelper.makeJsonParams("getappversion",
                new String[]{},
                new Object[]{}), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String msg = e.getMessage();
                if (msg.startsWith("Failed")) {
                    msg = "Unable to connect to the server，Please check the network";
                }
                ToastUtil.show(SettingActivity.this, msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                    String version_code = JsonUtil.getObjectByKey("version_code", result);
                    String version_url = JsonUtil.getObjectByKey("version_url", result);

                    if (version_code != null && version_url != null) {
                        PackageManager packageManager = SettingActivity.this.getPackageManager();
                        try {
                            PackageInfo packageInfo = packageManager.getPackageInfo(SettingActivity.this.getPackageName(), 0);
                            String versionCode = String.valueOf(packageInfo.versionCode);
                            if (!versionCode.equals(version_code)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.show(SettingActivity.this, "Discover new");
                                    }
                                });
                                update(version_url);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.show(SettingActivity.this, "The current version is the latest");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(SettingActivity.this, "The current version is the latest");
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(SettingActivity.this, "The current version is the latest");
                        }
                    });
                }
            }
        });
    };


    private void update(String url) {
        OkHttpHelper.download(url, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed")) {
                            msg = "Unable to connect to the server，Please check the network";
                        }
                        ToastUtil.show(SettingActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buff = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File apkFile = new File(sdPath, "nky.apk");
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    fos = new FileOutputStream(apkFile);

                    long sum = 0;
                    while ((len = is.read(buff)) != -1) {
                        fos.write(buff, 0, len);
                        sum += len;
                        int progress = (int) ((sum * 1.0f / total) * 100);
                        Message message = mUpdateApkHandler.obtainMessage();
                        message.what = 1;
                        message.arg1 = progress;
                        mUpdateApkHandler.sendMessage(message);
                    }
                    Message message = mUpdateApkHandler.obtainMessage();
                    message.what = 2;
                    message.obj = apkFile.getAbsolutePath();
                    mUpdateApkHandler.sendMessage(message);

                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            }
        });
    }
}