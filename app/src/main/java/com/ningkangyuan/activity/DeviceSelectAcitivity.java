package com.ningkangyuan.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Axi;
import com.ningkangyuan.bioland.DeviceControlActivity;
import com.ningkangyuan.kpi.Measure;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ScreenUtil;
import com.ningkangyuan.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuchun on 2016/9/15.
 */
public class DeviceSelectAcitivity extends BaseActivity {

    private static final String TAG = "DeviceSelectAcitivity";


    private GridView mGridView;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private List<Axi> mAxiList;
    private String mDeviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mGridView;
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("Check card number：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.device_select, null));

        mAxiList  = new ArrayList<Axi>();
        mAxiList.add(new Axi("blood pressure","BPM", 0));
        mAxiList.add(new Axi("blood sugar","BGM", 0));

        mGridView = (GridView) findViewById(R.id.device_select_list);
        mGridView.setAdapter(new DeviceSelectAdapter(this));



        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtil.show(this,"Bluetooth module was not detected.");
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String macName = device.getName();
            LogUtil.d(TAG,"macName：" + macName);
            if (macName != null && macName.startsWith("Bioland") && macName.endsWith(mDeviceType)) {
                dismissProgressDialog();
                String macAddress = device.getAddress();
                LogUtil.d(TAG, "macAddress：" + macAddress);
                Intent intent = new Intent(DeviceSelectAcitivity.this, DeviceControlActivity.class);
                intent.putExtra("DEVICE_NAME",macName);
                intent.putExtra("DEVICE_ADDRESS",macAddress);
                startActivity(intent);
                mBluetoothAdapter.stopLeScan(leScanCallback);
            }
        }
    };

    View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag(R.id.position);
            String name = mAxiList.get(position).getName();
            mDeviceType = mAxiList.get(position).getCode();
            showProgressDialog("Scanning" + name + "equipment,请确保该equipment已打开");
            mBluetoothAdapter.startLeScan(leScanCallback);
        }
    };

    private class DeviceSelectAdapter extends BaseAdapter {

        private Context context;
        private int itmeHeight;

        public DeviceSelectAdapter(Context context) {
            this.context = context;
            this.itmeHeight = ScreenUtil.dip2px(context, 80);
        }

        @Override
        public int getCount() {
            return mAxiList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.device_item,null);
                AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, itmeHeight);
                convertView.setLayoutParams(layoutParams);
                viewHolder = new ViewHolder();
                viewHolder.contentTV = (TextView) convertView.findViewById(R.id.device_item_name);
                convertView.setTag(R.id.holder,viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.id.holder);
            }

            viewHolder.contentTV.setText(mAxiList.get(position).getName());

            convertView.setTag(R.id.position, position);
            convertView.setOnClickListener(onItemClickListener);

            return convertView;
        }
    }

    private class ViewHolder {
        private TextView contentTV;
    }
}