package com.ningkangyuan.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.bean.DetectionKPI;
import com.ningkangyuan.bean.Order;
import com.ningkangyuan.bioland.DeviceControlActivity;
import com.ningkangyuan.kpi.Measure;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.storage.Shared;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.view.MainUpView;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/15.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "MainActivity";

    private TextView mCheckCardTV;

    private ImageView mStatusIV;

//    private ImageView mUserPortraitIV;
    private TextView mCheckUserTV,mCheckTimeTV,mCheckTypeTV,mCheckValueTV;

    private TextView mRegistrationInfoTV,mReferenceTV;
    private ImageView mErWeiMaIV;

    private FrameLayout mDetectionKpiContentFL;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private MainUpView mMainUpView;

    private Timer mTimer = new Timer(true);
    private TimerTask mGetInfoTimerTask;
    private TimerTask mScanBluetoothTask;

    private int mRequestFailure = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mCheckCardTV;
    }

    @Override
    protected void init() {
        mCheckCardTV = (TextView) findViewById(R.id.universal_checkcard_num);


        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.main, null));
        mMainUpView = (MainUpView) findViewById(R.id.main_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mStatusIV = (ImageView) findViewById(R.id.main_status);

//        mUserPortraitIV = (ImageView) findViewById(R.id.main_user_protrait);
        mCheckUserTV = (TextView) findViewById(R.id.main_check_user);
        mCheckTimeTV = (TextView) findViewById(R.id.main_check_time);
        mCheckTypeTV = (TextView) findViewById(R.id.main_check_type);
        mCheckValueTV = (TextView) findViewById(R.id.main_check_value);

        mReferenceTV = (TextView) findViewById(R.id.main_reference);
        mRegistrationInfoTV = (TextView) findViewById(R.id.main_registration_info);
        findViewById(R.id.main_registration_info_layout).setOnClickListener(this);
        findViewById(R.id.main_registration_info_layout).setOnFocusChangeListener(this);
        mErWeiMaIV = (ImageView) findViewById(R.id.main_erweima);

        mDetectionKpiContentFL = (FrameLayout) findViewById(R.id.main_detection_kpi_content);

        findViewById(R.id.main_remote).setOnClickListener(this);
        findViewById(R.id.main_registration).setOnClickListener(this);
        findViewById(R.id.main_my).setOnClickListener(this);
        findViewById(R.id.main_children).setOnClickListener(this);
        findViewById(R.id.main_message).setOnClickListener(this);
        findViewById(R.id.main_history).setOnClickListener(this);
        findViewById(R.id.main_setting).setOnClickListener(this);
        findViewById(R.id.main_guide).setOnClickListener(this);

        findViewById(R.id.main_registration).setOnFocusChangeListener(this);
        findViewById(R.id.main_remote).setOnFocusChangeListener(this);
        findViewById(R.id.main_my).setOnFocusChangeListener(this);
        findViewById(R.id.main_children).setOnFocusChangeListener(this);
        findViewById(R.id.main_message).setOnFocusChangeListener(this);
        findViewById(R.id.main_history).setOnFocusChangeListener(this);
        findViewById(R.id.main_setting).setOnFocusChangeListener(this);
        findViewById(R.id.main_guide).setOnFocusChangeListener(this);

        findViewById(R.id.main_my).requestFocus();

        mCheckCardTV.setText("检查卡号" + mVip.getCard_code());
        String userName = mVip.getReal_name();
        if (TextUtils.isEmpty(userName)) {
            userName = mVip.getMobile();
        }
        mCheckUserTV.setText(userName);
//        ImageLoaderHelper.getInstance().loader(mVip.getHeard_img_url(), mUserPortraitIV, ImageLoaderHelper.makeImageOptions());

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtil.show(this,"未检测到本设备的蓝牙模块");
        }

        showProgressDialog("正在查询测量数据..");
        qryOrder();
        getLastInfo();
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String macName = device.getName();
            LogUtil.d(TAG,"macName：" + macName);
            if (macName != null && macName.startsWith("Bioland")) {
                mBluetoothAdapter.stopLeScan(leScanCallback);
//                showBluetoothScanDialog(device);
                Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                intent.putExtra("DEVICE_NAME", device.getName());
                intent.putExtra("DEVICE_ADDRESS", device.getAddress());
                startActivityForResult(intent,0);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (data != null) {
                List<DetectionKPI> tempList = (List<DetectionKPI>) data.getSerializableExtra("data");
                showData(tempList);
            }
        }
    }

    private void excuteGetInfoTimer() {
        if (mGetInfoTimerTask == null) {
            mGetInfoTimerTask = new TimerTask() {
                @Override
                public void run() {
                    qryOrder();
                    getLastInfo();
                }
            };
        }
        mTimer.schedule(mGetInfoTimerTask,30 * 60 * 1000,38 * 60 * 1000);

        if (mScanBluetoothTask == null) {
            mScanBluetoothTask = new TimerTask() {
                @Override
                public void run() {
                    mBluetoothAdapter.startLeScan(leScanCallback);
                }
            };
        }
        mTimer.schedule(mScanBluetoothTask,8 * 1000);
    }

    private void stopGetInfoTimer() {
        if (mGetInfoTimerTask != null) {
            mGetInfoTimerTask.cancel();
            mGetInfoTimerTask = null;
        }
        if (mScanBluetoothTask != null) {
            mScanBluetoothTask.cancel();
            mScanBluetoothTask = null;
        }
    }

//    private AlertDialog.Builder mBluetoothScanDialog;
//    private void showBluetoothScanDialog(final BluetoothDevice device) {
//        if (mBluetoothScanDialog == null) {
//            mBluetoothScanDialog = new AlertDialog.Builder(this);
//            mBluetoothScanDialog.setTitle("提示");
//            mBluetoothScanDialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
//                    intent.putExtra("DEVICE_NAME", device.getName());
//                    intent.putExtra("DEVICE_ADDRESS", device.getAddress());
//                    startActivity(intent);
//                }
//            });
//            mBluetoothScanDialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    //再次去扫描
//                    mBluetoothAdapter.startLeScan(leScanCallback);
//                }
//            });
//        }
//        String message = device.getName();
//        if (message.endsWith("BPM")) {
//            message = "血压";
//        } if (message.endsWith("BGM")) {
//            message = "血糖";
//        }
//        mBluetoothScanDialog.setMessage("已检测到" + message + "设备，是否进行测量？");
//        mBluetoothScanDialog.show();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        excuteGetInfoTimer();
        if (MyApplication.isRefreshMain) {
            showProgressDialog("正在查询测量数据..");
            mVip = Shared.getInstance().getLocalVip(this);
            mCheckCardTV.setText("检查卡号" + mVip.getCard_code());
            String userName = mVip.getReal_name();
            if (TextUtils.isEmpty(userName)) {
                userName = mVip.getMobile();
            }
            mCheckUserTV.setText(userName);
            qryOrder();
            getLastInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRequestFailure = 0;
        mBluetoothAdapter.stopLeScan(leScanCallback);
        stopGetInfoTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mMainUpView.setFocusView(v,1.0f);
        } else {
            mMainUpView.setUnFocusView(v);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_remote:
                //远程咨询
            {
                Intent intent = new Intent(this,RemoteActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_registration:
                //挂号
            {
                Intent intent = new Intent(this,HosActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_my:
                //个人中心
            {
                Intent intent = new Intent(this,PersonalCenterActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_children:
                //成员管理
            {
                Intent intent = new Intent(this,FamilyActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_message:
                //消息
            {
                Intent intent = new Intent(this,MessageListActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_history:
                //检测历史
            {
                Intent intent = new Intent(this,HistoryActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_setting:
                //系统设置
            {
//                ToastUtil.show(this,"暂无此功能");
                Intent intent = new Intent(this,SettingActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_guide:
                //操作指南
//                ToastUtil.show(this,"暂无此功能");
            {
                Intent intent = new Intent(this,GuideActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.main_registration_info_layout:
            {
                Intent intent = new Intent(this,OrderActivity.class);
                startActivity(intent);
            }
                break;
        }
    }

    //查询挂号信息
    private void qryOrder() {
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("getghorderlst",
                new String[]{"orderId","status","hosid","vipcode","docid","deptid","patientname","pageIndex","pageSize"},
                new Object[]{"","","",mVip.getVip_code(),"","","",1, 1}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = e.getMessage();
                        if (msg.startsWith("Failed"))  {
                            msg = "无法连接服务器，请检查网络";
                        }
                        ToastUtil.show(MainActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                    String orders = JsonUtil.getObjectByKey("orders", result);
                    final List<Order> tempList = JsonUtil.mGson.fromJson(orders, new TypeToken<List<Order>>() {}.getType());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!tempList.isEmpty()) {
                                findViewById(R.id.main_no_reg).setVisibility(View.GONE);
                                Order order = tempList.get(0);
                                mRegistrationInfoTV.setText("预诊时间：" + order.getOutpdate() + "\n订单号：" + order.getOrderid() + "\n挂号费：" + order.getOrderfee() + "元\n医院："
                                 + order.getHosname() + "\n科室：" + order.getDeptname() + "\n医生：" + order.getDocname());
                            } else {
                                findViewById(R.id.main_no_reg).setVisibility(View.VISIBLE);
                                mRegistrationInfoTV.setText("暂无挂号信息");
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.main_no_reg).setVisibility(View.VISIBLE);
                            mRegistrationInfoTV.setText("暂无挂号信息");
                        }
                    });
                }
            }
        }));
    }

    private void getLastInfo() {
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("recentmeasuredata",
                new String[]{"card_code"},
                new Object[]{mVip.getCard_code()}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRequestFailure ++;
                        if (mRequestFailure < 3) {
                            getLastInfo();
                            return;
                        }
                        dismissProgressDialog();
//                        showEmptyView();
//                        String msg = e.getMessage();
//                        if (msg.startsWith("Failed"))  {
//                            msg = "无法连接服务器，请检查网络";
//                        }
                        ToastUtil.show(MainActivity.this, "无法连接服务器，请检查网络");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG,"onResponse：" + result);
                String flag = JsonUtil.getObjectByKey("flag",result);
                if ("success".equals(flag)) {
                    String array = JsonUtil.getObjectByKey("array",result);
                    final List<DetectionKPI> tempList = JsonUtil.mGson.fromJson(array,new TypeToken<List<DetectionKPI>>() {}.getType());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.isRefreshMain = false;
                            dismissProgressDialog();
                            showData(tempList);
                        }
                    });
                } else {
                    final String remark = JsonUtil.getObjectByKey("remark",result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            showEmptyView();
                            ToastUtil.show(MainActivity.this,remark);
                        }
                    });
                }
            }
        }));
    }

    private void showData(List<DetectionKPI> detectionKPIs) {
        if (detectionKPIs == null || detectionKPIs.isEmpty()) {
            showEmptyView();
            return;
        }
        DetectionKPI detectionKPI = detectionKPIs.get(0);
        String inspect_code = detectionKPI.getInspect_code();
        if (Measure.XueTang.INSPECT_CODE.equals(inspect_code)) {
            showXueTang(detectionKPIs);
        } else if (Measure.XueYa.INSPECT_CODE.equals(inspect_code)) {
            showXueYa(detectionKPIs);
        } else if (Measure.XingTi.INSPECT_CODE.equals(inspect_code)) {
            showXingTi(detectionKPIs);
        } else {
            showEmptyView();
        }
    }

    private TextView mHighPressTV,mLowPressTV,mPulseRateTV;
    private View mXueYaView;
    private void showXueYa(List<DetectionKPI> detectionKPIList) {

        if (mXueYaView == null) {
            mXueYaView = LayoutInflater.from(this).inflate(R.layout.main_xueya,null);
            mHighPressTV = (TextView) mXueYaView.findViewById(R.id.main_xueya_highpress);
            mLowPressTV = (TextView) mXueYaView.findViewById(R.id.main_xueya_lowpress);
            mPulseRateTV = (TextView) mXueYaView.findViewById(R.id.main_xueya_pulserate);
        }
        mDetectionKpiContentFL.removeAllViews();
        mDetectionKpiContentFL.addView(mXueYaView);

        for(DetectionKPI detectionKPI : detectionKPIList) {
            if (Measure.XueYa.CODE_PR.equals(detectionKPI.getKpi_code())) {
                mPulseRateTV.setText(detectionKPI.getInspect_value());
            } else if (Measure.XueYa.CODE_SYS.equals(detectionKPI.getKpi_code())) {
                mHighPressTV.setText(detectionKPI.getInspect_value());
            } else if (Measure.XueYa.CODE_DIA.equals(detectionKPI.getKpi_code())) {
                mLowPressTV.setText(detectionKPI.getInspect_value());
            }
        }
        showCheckInfo(detectionKPIList);
    }

    private TextView mValueTV,mTypeTV;
    private View mXueTangView;
    private void showXueTang(List<DetectionKPI> detectionKPIList) {
        if (mXueTangView == null) {
            mXueTangView = LayoutInflater.from(this).inflate(R.layout.main_xuetang,null);
            mTypeTV = (TextView) mXueTangView.findViewById(R.id.main_xuetang_type);;
            mValueTV = (TextView) mXueTangView.findViewById(R.id.main_xuetang_value);
        }
        mDetectionKpiContentFL.removeAllViews();
        mDetectionKpiContentFL.addView(mXueTangView);

        for(DetectionKPI detectionKPI : detectionKPIList) {
            String str = "";
            if (Measure.XueTang.CODE_GLU0.equals(detectionKPI.getKpi_code())) {
                str = "随机血糖";
            } else if (Measure.XueTang.CODE_GLU1.equals(detectionKPI.getKpi_code())) {
                str = "餐前血糖";
            } else if (Measure.XueTang.CODE_GLU2.equals(detectionKPI.getKpi_code())) {
                str = "餐后血糖";
            }
            mTypeTV.setText(str);
            mValueTV.setText(detectionKPI.getInspect_value());
        }
        showCheckInfo(detectionKPIList);
    }

    private TextView mHeightTV,mWeightTV,mBMITV;
    private View mXingTiView;
    private void showXingTi(List<DetectionKPI> detectionKPIList) {

        if (mXingTiView == null) {
            mXingTiView = LayoutInflater.from(this).inflate(R.layout.main_xingti,null);
            mHeightTV = (TextView) mXingTiView.findViewById(R.id.main_xingti_height);
            mWeightTV = (TextView) mXingTiView.findViewById(R.id.main_xingti_weight);
            mBMITV = (TextView) mXingTiView.findViewById(R.id.main_xingti_bmi);
        }
        mDetectionKpiContentFL.removeAllViews();
        mDetectionKpiContentFL.addView(mXingTiView);

        for(DetectionKPI detectionKPI : detectionKPIList) {
            if (Measure.XingTi.HEIGHT.equals(detectionKPI.getKpi_code())) {
                mHeightTV.setText(detectionKPI.getInspect_value());
            } else if (Measure.XingTi.WEIGHT.equals(detectionKPI.getKpi_code())) {
                mWeightTV.setText(detectionKPI.getInspect_value());
            } else if (Measure.XingTi.BMI.equals(detectionKPI.getKpi_code())) {
                mBMITV.setText(detectionKPI.getInspect_value());
            }
        }
        showCheckInfo(detectionKPIList);
    }

    private void showCheckInfo(List<DetectionKPI> detectionKPIList) {
        int status = 0;
        for (int i = 0; i < detectionKPIList.size();i ++) {
            if (!"0".equals(detectionKPIList.get(i).getInspect_is_normal())) {
                status = Integer.valueOf(detectionKPIList.get(i).getInspect_is_normal());
                break;
            }
        }
        if (status == -1) {
            status = R.drawable.main_check_status_low;
        } else if (status == 0) {
            status = R.drawable.main_check_status_normal;
        } else if (status == 1) {
            status = R.drawable.main_check_status_high;
        }
        mStatusIV.setImageResource(status);

        String desc = "参考阈值  ";
        for (int i = 0; i < detectionKPIList.size();i ++) {
            desc += " " + detectionKPIList.get(i).getInspect_desc();
        }
        mReferenceTV.setText(desc);

        DetectionKPI detectionKPI = detectionKPIList.get(0);
        String inspect_time = detectionKPI.getInspect_time();
        mCheckTimeTV.setText("最近一次检测：" + inspect_time);
        String type = "";
        String value = "";
        if (Measure.XueTang.INSPECT_CODE.equals(detectionKPI.getInspect_code())) {
            type = "血糖";
            value = detectionKPI.getInspect_value();
        } else if (Measure.XueYa.INSPECT_CODE.equals(detectionKPI.getInspect_code())) {
            type = "血压";
            value = mHighPressTV.getText() + "/" + mLowPressTV.getText();
        } else if (Measure.XingTi.INSPECT_CODE.equals(detectionKPI.getInspect_code())) {
            type = "形体";
            value = mHeightTV.getText() + "/" + mWeightTV.getText();
        }
        mCheckTypeTV.setText(type);
        mCheckValueTV.setText(value);
    }

    private View mEmptyView;
    private void showEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = LayoutInflater.from(this).inflate(R.layout.main_xueya,null);
        }
        mDetectionKpiContentFL.removeAllViews();
        mDetectionKpiContentFL.addView(mEmptyView);

        mCheckTimeTV.setText("最近一次检测时间：--");
        mCheckTypeTV.setText("血压");
        mCheckValueTV.setText("--");
        mReferenceTV.setText("");
    }

}
