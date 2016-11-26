package com.ningkangyuan.receive;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.ningkangyuan.utils.LogUtil;

/**
 * Created by xuchun on 2016/9/6.
 */
public class BluetoothScanService extends Service {

    private static final String TAG = "ScanService";

    private String mMacName;
    private String mMacAddress;

    private BluetoothBinder mBluetoothBinder;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    //只在第一次被创建的时候调用
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate");

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            LogUtil.d(TAG, "mBluetoothManager is null");
            return;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.d(TAG, "mBluetoothAdapter is null");
            return;
        }

        registerBluetoothReceive();

        mBluetoothBinder = new BluetoothBinder();

        if (mBluetoothAdapter.isEnabled()) {
            //已打开就直接扫描
            mBluetoothAdapter.startDiscovery();
            return;
        }
        //去打开蓝牙
        mBluetoothAdapter.enable();
//        openBluetooth();

    }

    //只要执行startservice此方法都会被调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand");


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onStartCommand");
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(bluetoothReceive);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBluetoothBinder;
    }

    private void registerBluetoothReceive() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceive, intentFilter);
    }

    private void openBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class BluetoothBinder extends Binder {

        public String getMacName() {
            return mMacName;
        }

        public String getMacAddress() {
            return mMacAddress;
        }


        public void clear() {
            mMacName = null;
            mMacAddress = null;
        }
    }

    private BroadcastReceiver bluetoothReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String macName =bluetoothDevice.getName();
                String macAddress = bluetoothDevice.getAddress();
                if (macName != null && macName.startsWith("Bioland")) {
                    mMacName = macName;
                    mMacAddress = macAddress;
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //扫描结束继续新一轮的扫描
                LogUtil.d(TAG,"ACTION_DISCOVERY_FINISHED");
                mBluetoothAdapter.startDiscovery();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                //蓝牙打开
                if (state == BluetoothAdapter.STATE_ON) {
                    //直接扫描
                    mBluetoothAdapter.startDiscovery();
                }
            }
        }
    };

}