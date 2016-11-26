package com.ningkangyuan.activity;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.push.BDPushReceiver;
import com.ningkangyuan.storage.Shared;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.NetworkUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 此activity做一些项目初始化的操作
 * Created by xuchun on 2016/8/15.
 */
public class InitActivity extends Activity {

    private static final String TAG = "InitActivity";

    //企业
    private static final String PUSH_API_KEY = "iO2Kd9I9Sv2Cn4Djmm8YFBaD";

    //我的
//    private static final String PUSH_API_KEY = "kuE3h2aj59tuyoVlttTxI4ZO";

    private ProgressBar mProgressBar;

    private Timer mTimer = new Timer(true);

    private String mAccount;
    private String mPassword;

    private boolean mIsCheckUpdate = false;

    private boolean mIsTimeOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init);
        mAccount = Shared.getInstance().getIdentityStr(this);
        mPassword = Shared.getInstance().getPasswordStr(this);
        init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownLoadCall != null) {
            mDownLoadCall.cancel();
        }
    }

    private void init() {
        mProgressBar = (ProgressBar) findViewById(R.id.init_loading);

        if (!NetworkUtil.isHaveNet(this)) {
            ToastUtil.show(this,"Network is not available");
            finish();
            return;
        }

        //初始化百度push
        initBDPush();

        initBluetooth();

//        获取服务器时间更新本地时间
        updateSystemDate();

        //检查版本更新
        checkNewVersion();

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mIsTimeOver = true;
                if (mIsCheckUpdate) {
                    leave();
                }
//                leave();
            }
        }, 3 * 1000);
    }

    //初始化蓝牙
    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter() == null) {
            ToastUtil.show(this,"No Bluetooth module detected");
            return;
        }
        bluetoothManager.getAdapter().enable();
    }

    private void initBDPush() {
//        if (PushManager.isPushEnabled(MyApplication.mContext)) {
//            return;
//        }
        PushManager.startWork(MyApplication.mContext, PushConstants.LOGIN_TYPE_API_KEY, PUSH_API_KEY);
    }

    private void leave() {
        if (mAccount == null || mPassword == null) {
            //这里跳转
            Intent intent = new Intent(InitActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();;
        } else {
            login();
        }
    }


//    private void initRLYVoip() {
//        Vip vip = Shared.getInstance().getLocalVip(this);
//        if (vip == null) {
//            return;
//        }
//        //使用vip_code进行登录
//        VoipHelper.getInstance().login(vip.getVip_code());
//    }

    //获取服务器时间更新本地系统时间
    private void updateSystemDate() {
        OkHttpHelper.get(OkHttpHelper.makeJsonParams("getsystemtime", new String[]{}, new Object[]{}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed"))  {
                            msg = "Unable to connect to the server，Please check the network";
                        }
                        ToastUtil.show(InitActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                LogUtil.d(TAG, result);
                final String code = JsonUtil.getObjectByKey("code", result);
                if ("1".equals(code)) {
                    final String time = JsonUtil.getObjectByKey("datetime", result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Process process = Runtime.getRuntime().exec("su");
//                                String datetime="20160917.102800"; //测试的设置的时间【时间格式 yyyyMMdd.HHmmss】
                                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                os.writeBytes("setprop persist.sys.timezone GMT\n");
                                os.writeBytes("/system/bin/date -s " + time + "\n");
                                os.writeBytes("clock -w\n");
                                os.writeBytes("exit\n");
                                os.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void login() {
        OkHttpHelper.get(OkHttpHelper.makeJsonParams("userlogin",
                new String[]{"num","password","android_tv_channel_id"},
                new Object[]{mAccount,mPassword, BDPushReceiver.android_tv_channel_id}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed"))  {
                            msg = "Unable to connect to the server，Please check the network";
                        }
                        ToastUtil.show(InitActivity.this, msg);
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
                        ToastUtil.show(InitActivity.this, message);
                        Intent intent = new Intent();
                        if ("1".equals(code)) {
                            //将登陆数据存起来
                            getSharedPreferences("login", 0).edit().putString("vip", vip_info).commit();

                            //跳转至主页面
                            intent.setClass(InitActivity.this,MainActivity.class);
                        } else {
                            intent.setClass(InitActivity.this,LoginActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private Handler mUpdateApkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int progress = msg.arg1;
                    ToastUtil.show(InitActivity.this,"Download progress:" + progress + "%");
                    break;
                case 2:
                    String apkPath = (String) msg.obj;
                    ToastUtil.show(InitActivity.this,"Download completed，To install");
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
                if (msg.startsWith("Failed"))  {
                    msg = "Unable to connect to the server，Please check the network";
                }
                ToastUtil.show(InitActivity.this, msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                    String version_code = JsonUtil.getObjectByKey("version_code", result);
                    String version_url = JsonUtil.getObjectByKey("version_url", result);

                    if (version_code != null && version_url != null) {
                        PackageManager packageManager = InitActivity.this.getPackageManager();
                        try {
                            PackageInfo packageInfo = packageManager.getPackageInfo(InitActivity.this.getPackageName(), 0);
                            String versionCode = String.valueOf(packageInfo.versionCode);
                            if (!versionCode.equals(version_code)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.show(InitActivity.this,"Discover new");
                                    }
                                });
                                update(version_url);
                            } else {
                                mIsCheckUpdate = true;
                                if (mIsTimeOver) {
                                    leave();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mIsCheckUpdate = true;
                        if (mIsTimeOver) {
                            leave();
                        }
                    }
                } else {
                    mIsCheckUpdate = true;
                    if (mIsTimeOver) {
                        leave();
                    }
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
                        ToastUtil.show(InitActivity.this, msg);
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