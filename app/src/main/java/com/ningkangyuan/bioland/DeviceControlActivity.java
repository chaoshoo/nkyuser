/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ningkangyuan.bioland;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.activity.BaseActivity;
import com.ningkangyuan.bean.DetectionKPI;
import com.ningkangyuan.kpi.Measure;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ScreenUtil;
import com.ningkangyuan.utils.ToastUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends BaseActivity implements OnClickListener {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();
	/**
	 * 血压
	 */
	private final static int BP = 1;
	/**
	 * 血糖
	 */
	private final static int GLU = 2;
	// private final static int BP = 1;
	private PowerManager pm;
	private PowerManager.WakeLock mWakeLock;

	private Commands commands;
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mConnectionState;
	private TextView mDataField, mSendDataField, txt_message;
//	private String mDeviceName;
	private String mDeviceAddress;
//	private ExpandableListView mGattServicesList;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;

//	private final String LIST_NAME = "NAME";
//	private final String LIST_UUID = "UUID";

	private BluetoothGatt mBluetoothGatt;
	// private EditText edt_message;
	private Button btn_bs_send, btn_bp_send;
	private String notifyString = "";
	private int deviceType;
	private byte[] sendDataByte;
//	private byte[] getProcessData = new byte[8];

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {

			mHandler.sendEmptyMessage(2);
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				displayData(notifyString);
				break;
			case 1:
				// 显示发送的命令值
				StringBuilder builder = new StringBuilder(sendDataByte.length);
				for (byte b : sendDataByte) {
					builder.append(String.format("%02X ", b));
				}
				displaySendData(builder.toString());

				break;
			case 2:

				// 此处要连接发送两次命令，不然血压收不到过程和结果包．
				sendDataByte();
				// sendDataByte();
				break;

			default:
				break;
			}

		}

	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}

			if (mBluetoothLeService.connect(mDeviceAddress)) {
				mBluetoothGatt = mBluetoothLeService.getGatt();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	//
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected, Color.GREEN);
//				invalidateOptionsMenu();

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				mConnected = false;
				dismissProgressDialog();
				updateConnectionState(R.string.disconnected, Color.RED);
//				invalidateOptionsMenu();
//				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {

				if (mBluetoothGatt != null) {
//					displayGattServices(mBluetoothGatt.getServices());
					btn_bp_send.setEnabled(true);
					mHandler.postDelayed(mRunnable, 2000);
				}

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				// 获取设备上传的数据

				byte[] notify = intent
						.getByteArrayExtra(BluetoothLeService.EXTRA_NOTIFY_DATA); // Log.e(TAG,
																					// //
																					// "有数据回来了"

				if (notify != null) {

					getData(notify);

				}
			}
		}
	};

	ArrayList<Byte> dataPackage = new ArrayList<Byte>();

	/**
	 * 校验命令包
	 */
	private void checkDataPackage(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			dataPackage.add(data[i]);
		}

	}

	private void getData(byte notify[]) {
		Log.e(TAG, "227=" + notify.length);
		if (notify[0] == 0x55) {
			if (notify.length >= 10 && notify[1] == 0x10 && notify[2] == 0) {
				deviceType = notify[5];// 得到机种号：blood pressure1；blood sugar2；
				mHandler.postDelayed(mRunnable, 300);
			}
			if (deviceType == 1) {
				if (notify.length == 6 && notify[1] == 0x06) {
					if (notify[2] == 1) {// 开始包
						mHandler.postDelayed(mRunnable, 300);
					} else if (notify[2] == 4) {// 结束包
						// Log.e(TAG, "238我收到结束包了");
					}
				} else if (notify.length >= 13 && notify[1] == (byte) 0x0f
						&& notify[2] == 3) {
					// 血压的结果包
					Log.e(TAG, "结果");
					getDeviceData(deviceType, notify);
					sendDataByte();
				}

			} else if (deviceType == 2) {
				if (notify[1] == 14 && notify[2] == 3) {
					// 血糖的结果包
					getDeviceData(deviceType, notify);
					sendDataByte();
				}
			}

		}

		StringBuilder builder = new StringBuilder(notify.length);
		for (byte b : notify) {
			builder.append(String.format("%02X ", b));
		}
		notifyString = builder.toString();
		mHandler.sendEmptyMessage(0);
	}

//	private void clearUI() {
//		mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
//		// mDataField.setText(R.string.no_data);
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.universal);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");

		final Intent intent = getIntent();
//		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		commands = new Commands();

		((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.gatt_services_characteristics, null));
		((TextView)findViewById(R.id.universal_checkcard_num)).setText("Check card number" + mVip.getCard_code());
		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
//		mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);

		mSendDataField = (TextView) findViewById(R.id.data_send_value);
		txt_message = (TextView) findViewById(R.id.txt_message);
		txt_message.setMovementMethod(ScrollingMovementMethod.getInstance());

		/**
		 * 血糖
		 */

		// btn_bs_send = (Button) findViewById(R.id.btn_bs_send);
		/**
		 * 血压
		 */
		btn_bp_send = (Button) findViewById(R.id.btn_bp_send);

		// btn_bs_send.setOnClickListener(this);
		btn_bp_send.setOnClickListener(this);
//		getActionBar().setTitle(mDeviceName);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId, final int color) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
				mConnectionState.setTextColor(color);
			}
		});
	}

	/**
	 * 显示设备上传的数据；因有多个包数据，在包后用;号隔开．
	 * 
	 * @param data
	 */
	private void displayData(String data) {
		if (data != null) {
			mDataField.setText(data);
		}
	}

	private void displaySendData(String data) {
		if (data != null) {
			mSendDataField.setText(data);
		}
	}

	/**
	 * 转换后的数据
	 */
	private void displayTransformationData(String data) {
		// StringBuffer
		txt_message.setText(txt_message.getText() + "\n" + data);
		// sendDataByte();

	}

//	private void displayGattServices(List<BluetoothGattService> gattServices) {
//		if (gattServices == null)
//			return;
//		String uuid = null;
//		String UUIDServiceString = getResources().getString(
//				R.string.uuid_service);
//		String UUIDWriteString = getResources().getString(R.string.uuid_write);
//		String UUIDReadString = getResources().getString(R.string.uuid_read);
//		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
//		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
//		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//
//		// Loops through available GATT Services.
//		for (BluetoothGattService gattService : gattServices) {
//			HashMap<String, String> currentServiceData = new HashMap<String, String>();
//			uuid = gattService.getUuid().toString();
//			if (uuid.equals(SampleGattAttributes.GATT_SERVICE_PRIMARY)) {
//				currentServiceData.put(LIST_NAME, UUIDServiceString);
//				currentServiceData.put(LIST_UUID, uuid);
//			} else {
//				continue;
//			}
//			gattServiceData.add(currentServiceData);
//
//			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
//			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
//					.getCharacteristics();
//			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
//
//			// Loops through available Characteristics.
//			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//				charas.add(gattCharacteristic);
//				HashMap<String, String> currentCharaData = new HashMap<String, String>();
//				uuid = gattCharacteristic.getUuid().toString();
//				if (uuid.equals(SampleGattAttributes.CHARACTERISTIC_WRITEABLE)) {
//					currentCharaData.put(LIST_NAME, UUIDWriteString);
//					currentCharaData.put(LIST_UUID, uuid);
//
//				} else if (uuid
//						.equals(SampleGattAttributes.CHARACTERISTIC_NOTIFY)) {
//					currentCharaData.put(LIST_NAME, UUIDReadString);
//					currentCharaData.put(LIST_UUID, uuid);
//
//				}
//
//				gattCharacteristicGroupData.add(currentCharaData);
//			}
//			mGattCharacteristics.add(charas);
//			gattCharacteristicData.add(gattCharacteristicGroupData);
//		}
//
//		SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//				this, gattServiceData,
//				android.R.layout.simple_expandable_list_item_2, new String[] {
//						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
//						android.R.id.text2 }, gattCharacteristicData,
//				android.R.layout.simple_expandable_list_item_2, new String[] {
//						LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
//						android.R.id.text2 });
//		mGattServicesList.setAdapter(gattServiceAdapter);
//	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_bp_send:
			sendDataByte();
//			mHandler.sendEmptyMessage(1);
			break;
		}

	}

	/**
	 * 向设备发送命令
	 */
	private void sendDataByte() {

		sendDataByte = commands.getSystemdate(commands.CMD_HEAD,
				commands.CMD_LENGTH_ELEVEN, commands.CMD_CATEGORY_FIVE);
		boolean aa = SampleGattAttributes.sendMessage(mBluetoothGatt, sendDataByte);
		mHandler.sendEmptyMessage(1);
		Log.d("sendDataByte", "" + aa);
	}

	/**
	 * 根据不同的设备　显示测量的结果值
	 * 
	 * @param type
	 */
	private void getDeviceData(int type, byte[] b) {
		mType = type;
		mB = b;
		switch (type) {
		case BP:
			if (b.length > 8) {
				for (int i = 0; i < b.length; i++) {
					Log.e(TAG, "463===" + b[i]);
				}

				String resultBG = "high pressure：" + getShort(b, 8) + " low：" + b[10]
						+ " heart：" + b[11];
				displayTransformationData(resultBG);
				uploadResult();
			}
			break;
		case GLU:
			String result4 = getShort(b, 9) + "";
			displayTransformationData(swithXueTang(result4) + "mmol");
			showXueTangType();
			break;
		default:
			break;
		}

	}

	private List<DetectionKPI> mResultList;
	private int mType;
	private byte[] mB;
	private JSONObject makeUploadParams() {
		if (mResultList == null) {
			mResultList = new ArrayList<DetectionKPI>();
		}
		mResultList.clear();
        if (BP == mType) {
			//脉率
			DetectionKPI frKPI = new DetectionKPI();
			frKPI.setInspect_code(Measure.XueYa.INSPECT_CODE);
			frKPI.setKpi_code(Measure.XueYa.CODE_PR);
			frKPI.setInspect_is_normal("0");
			frKPI.setInspect_desc("");
			frKPI.setInspect_value("" + mB[11]);
			frKPI.setInspect_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			mResultList.add(frKPI);

			//收缩压
			DetectionKPI sysKPI = new DetectionKPI() ;
			sysKPI.setInspect_code(Measure.XueYa.INSPECT_CODE);
			sysKPI.setKpi_code(Measure.XueYa.CODE_SYS);
			sysKPI.setInspect_is_normal("0");
			sysKPI.setInspect_desc("");
			sysKPI.setInspect_value("" + getShort(mB, 8));
			mResultList.add(sysKPI);

			//舒张压
			DetectionKPI diaKPI = new DetectionKPI() ;
			diaKPI.setInspect_code(Measure.XueYa.INSPECT_CODE);
			diaKPI.setKpi_code(Measure.XueYa.CODE_DIA);
			diaKPI.setInspect_is_normal("0");
			diaKPI.setInspect_desc("");
			diaKPI.setInspect_value("" + mB[10]);
			mResultList.add(diaKPI);

            return OkHttpHelper.makeJsonParams("measure",
                    new String[]{"inspect_code","card_code","device_sn","inspect_time","SYS","DIA","PR"},
                    new Object[]{Measure.XueYa.INSPECT_CODE,mVip.getCard_code(),
							ScreenUtil.getDeviceId(this),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                            "" + getShort(mB, 8) ,
							"" + mB[10],
							"" + mB[11]});
        } else if (GLU == mType) {
			//血糖
			DetectionKPI xtKPI = new DetectionKPI() ;
			xtKPI.setInspect_code(Measure.XueTang.INSPECT_CODE);
			xtKPI.setKpi_code(mXueTangType);
			xtKPI.setInspect_is_normal("0");
			xtKPI.setInspect_desc("");
			xtKPI.setInspect_value(swithXueTang(getShort(mB, 9) + ""));
			xtKPI.setInspect_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			mResultList.add(xtKPI);
            return OkHttpHelper.makeJsonParams("measure",
                    new String[]{"inspect_code","card_code","device_sn","inspect_time",mXueTangType},
                    new Object[]{Measure.XueTang.INSPECT_CODE,mVip.getCard_code(),
							ScreenUtil.getDeviceId(this),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
							swithXueTang(getShort(mB, 9) + "")});
        }
        return new JSONObject();
    }

	private View.OnClickListener mXueTangSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.pop_xuetang_glu0:
					//随机血糖
					mXueTangType = "GLU0";
					break;
				case R.id.pop_xuetang_glu1:
					//餐前血糖
					mXueTangType = "GLU1";
					break;
				case R.id.pop_xuetang_glu2:
					//餐后血糖
					mXueTangType = "GLU2";
					break;
			}
			mXueTangSelect.dismiss();
            uploadResult();
		}
	};

	private String mXueTangType;
	private PopupWindow mXueTangSelect;
	private void showXueTangType() {
		if (mXueTangSelect == null) {

			View view = LayoutInflater.from(this).inflate(R.layout.pop_xuetang_type,null);
			int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
			view.findViewById(R.id.pop_xuetang_glu0).setOnClickListener(mXueTangSelectListener);
			view.findViewById(R.id.pop_xuetang_glu1).setOnClickListener(mXueTangSelectListener);
			view.findViewById(R.id.pop_xuetang_glu2).setOnClickListener(mXueTangSelectListener);

			mXueTangSelect = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
//            mXueTangSelect.setBackgroundDrawable(new ColorDrawable(0x00000000));
//            mXueTangSelect.setOutsideTouchable(false);
		}

		mXueTangSelect.showAtLocation(mConnectionState, Gravity.CENTER,0,0);
	}

	private void uploadResult() {
        showProgressDialog("Uploading data");
        mCallList.add(OkHttpHelper.get(makeUploadParams(), new Callback() {
			@Override
			public void onFailure(Call call, final IOException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dismissProgressDialog();
						String msg = e.getMessage();
						if (msg.startsWith("Failed")) {
							msg = "Unable to connect to the server，Please check the network";
						}
						ToastUtil.show(DeviceControlActivity.this, msg);
					}
				});
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String result = response.body().string();
				LogUtil.d(TAG, "onResponse：" + result);
				String flag = JsonUtil.getObjectByKey("flag", result);
				if ("success".equals(flag)) {
					MyApplication.isRefreshMain = true;
					final String inspect_is_normal = JsonUtil.getObjectByKey("inspect_is_normal", result);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							String status = "";
//							if ("-1".equals(inspect_is_normal)) {
//								status = "偏低";
//							} else if ("0".equals(inspect_is_normal)) {
//								status = "正常";
//							} else if ("1".equals(inspect_is_normal)) {
//								status = "偏高";
//							}
							dismissProgressDialog();
							if (mResultList != null && !mResultList.isEmpty()) {
								mResultList.get(0).setInspect_is_normal(inspect_is_normal);
							}
							Intent intent = new Intent();
							intent.putExtra("data", (Serializable) mResultList);
							setResult(RESULT_OK, intent);
							showConfirmMsg("Upload success，Please close device and click OK to exit。");
						}
					});
				} else {
					final String remark = JsonUtil.getObjectByKey("remark", result);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dismissProgressDialog();
							showConfirmMsg("Data upload failed：" + remark);
						}
					});
				}
			}
		}));
    }

	private AlertDialog.Builder mUploadOverHintDialog;
	private void showConfirmMsg(String msg) {
		if (mUploadOverHintDialog == null) {
			mUploadOverHintDialog = new AlertDialog.Builder(this);
			mUploadOverHintDialog.setTitle("Prompt");
			mUploadOverHintDialog.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
		}
		mUploadOverHintDialog.setMessage(msg);
		mUploadOverHintDialog.show();
	}

	public static short getShort(byte[] b, int index) {
		Log.e(TAG, "508==" + b[index + 1] + "==" + (b[index + 1] << 8) + "=="
				+ b[index]);
		return (short) (((b[index + 1] << 8) | b[index] & 0xff));
	}

	public String swithXueTang(String result) {
		double resultValue = Double.parseDouble(result);
		resultValue = resultValue / 18;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setRoundingMode(RoundingMode.HALF_UP);// 设置四舍五入
		nf.setMinimumFractionDigits(1);// 设置最小保留几位小数
		nf.setMaximumFractionDigits(1);// 设置最大保留几位小数
		return nf.format(resultValue);
	}
}