package com.ningkangyuan.activity;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Vip;
import com.ningkangyuan.nrtc.MultiChatActivity;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.push.BDPushReceiver;
import com.ningkangyuan.push.OnPushListener;
import com.ningkangyuan.storage.Shared;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ScreenUtil;
import com.ningkangyuan.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xuchun on 2016/8/15.
 */
public class BaseActivity extends FragmentActivity implements OnPushListener {

    private static final String TAG = "BaseActivity";

    protected static List<Call> mCallList = new ArrayList<Call>();

    protected ProgressDialog mProgressDialog;

    protected Vip mVip;

    protected boolean isLoadVip = true;

    private PopupWindow mMsgPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initProgressDialog();
        if (isLoadVip) {
            mVip = Shared.getInstance().getLocalVip(this);
            if (mVip == null) {
                ToastUtil.show(this,"请先登录");
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BDPushReceiver.mOnPushListener = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        BDPushReceiver.mOnPushListener = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        ToastUtil.destory();
        for (Call call : mCallList) {
            call.cancel();
        }
        mCallList.clear();
    }

    protected void init() {};

    protected View getView() {return null;};

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
    }

    protected void showProgressDialog(String str) {
        if (!TextUtils.isEmpty(str)) {
            mProgressDialog.setMessage(str);
        }
        mProgressDialog.show();
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private TextView mTitleTV,mContentTV;
    private Button mConfirm,mCancel;
    private String mPushMsg;
    @Override
    public void inMessage(String msg) {
        mPushMsg = msg;
        LogUtil.d(TAG,"msg：" + msg);
        if (isFinishing()) {
            return;
        }
        if (mMsgPW == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.pw_message,null);
            mConfirm = (Button) view.findViewById(R.id.pw_message_confirm);
            mCancel = (Button) view.findViewById(R.id.pw_message_cancel);
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goVedio(mPushMsg);
                }
            });
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMsgPW.dismiss();
                }
            });
            mTitleTV = (TextView) view.findViewById(R.id.pw_message_title);
            mContentTV = (TextView) view.findViewById(R.id.pw_message_content);

            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mMsgPW = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mMsgPW.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mMsgPW.setOutsideTouchable(false);
        }

        try {
            String titleStr = "";
            String msgStr = "";

            JSONObject jsonObject = new JSONObject(msg);
            String title = jsonObject.getString("title");
            String description = jsonObject.getString("description");

            titleStr = title;
            msgStr = description;
            mConfirm.setVisibility(View.GONE);
            mCancel.setText("确定");
            if ("vedio".equals(title)) {
                mConfirm.setVisibility(View.VISIBLE);
                mCancel.setText("取消");
                String doctorName = description.split(",")[2];
                titleStr = "视频请求";
                msgStr = doctorName + "请求与您视频，是否应答？";
            }
            mTitleTV.setText(titleStr);
            mContentTV.setText("" + msgStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (getView() != null) {
            mMsgPW.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        }

    }

    private void goVedio(final String msg) {
        showProgressDialog("正在连接服务器");
        OkHttpHelper.get(OkHttpHelper.makeJsonParams("getvediotoken",
                new String[]{"uid"},
                new Object[]{mVip.getId()}), new Callback() {
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
                        ToastUtil.show(BaseActivity.this, "" + msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                LogUtil.d(TAG, result);
                final String code = JsonUtil.getObjectByKey("code", result);
                if ("1".equals(code)) {
                    final String token = JsonUtil.getObjectByKey("token", result);

                    String description = JsonUtil.getObjectByKey("description", msg);
                    final String channel = description.split(",")[0];
                    final String remoteId = description.split(",")[1];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            mMsgPW.dismiss();
                            MultiChatActivity.launch(BaseActivity.this,
                                    Long.valueOf(mVip.getId()),
                                    channel,
                                    token,
                                    true,
                                    true,
                                    false,
                                    remoteId);
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(BaseActivity.this, "Token获取失败");
                    }
                });
            }
        });
    }
}
