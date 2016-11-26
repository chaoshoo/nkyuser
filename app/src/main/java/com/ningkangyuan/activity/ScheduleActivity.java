package com.ningkangyuan.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.Constant;
import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.ScheduleAdapter;
import com.ningkangyuan.bean.Schedule;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ScreenUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.bridge.RecyclerViewBridge;
import com.open.androidtvwidget.leanback.recycle.GridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.view.MainUpView;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 挂号医生
 * Created by xuchun on 2016/9/1.
 */
public class ScheduleActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "DocActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private List<Schedule> mScheduleList = new ArrayList<Schedule>();
    private ScheduleAdapter mScheduleAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;

    private Dialog mLockDialog;
    private PopupWindow mLockNumSuccessPop;

    private String mOutpdate;

    private String mHosid;
    private String mDeptid;
    private String mDocid;
    private String mScheduleid;
    private boolean isLastPage = false;
    private int mPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mRecyclerViewTV;
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("Check card number：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.schedule, null));

        mUpBtn = (Button) findViewById(R.id.schedule_up);
        mNextBtn = (Button) findViewById(R.id.schedule_next);
        mBackBtn = (Button) findViewById(R.id.schedule_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.schedule_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.schedule_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,2);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mScheduleAdapter = new ScheduleAdapter(mScheduleList));
        mRecyclerViewTV.setOnItemListener(new RecyclerViewTV.OnItemListener() {
            @Override
            public void onItemPreSelected(RecyclerViewTV parent, View itemView, int position) {
                mMainUpView.setUnFocusView(itemView);
            }

            @Override
            public void onItemSelected(RecyclerViewTV parent, View itemView, int position) {
                mMainUpView.setFocusView(itemView, 1.0f);
            }

            @Override
            public void onReviseFocusFollow(RecyclerViewTV parent, View itemView, int position) {
                mMainUpView.setFocusView(itemView, 1.0f);
            }
        });
        mRecyclerViewTV.setOnItemClickListener(new RecyclerViewTV.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerViewTV parent, View itemView, int position) {
                if ("7".equals(mScheduleList.get(position).getValidflag())) {
                    Intent intent = new Intent(ScheduleActivity.this,TimeScheduleActivity.class);
                    intent.putExtra("hosid",mHosid);
                    intent.putExtra("deptid",mDeptid);
                    intent.putExtra("docid",mDocid);
                    intent.putExtra("scheduleid",mScheduleList.get(position).getScheduleid());
                    intent.putExtra("outpdate",mScheduleList.get(position).getOutpdate());
                    startActivity(intent);
                    return;
                }
                if ("1".equals(mScheduleList.get(position).getValidflag())) {
                    mScheduleid = mScheduleList.get(position).getScheduleid();
                    mOutpdate = mScheduleList.get(position).getOutpdate();
                    showLockNum();
                    return;
                }
                ToastUtil.show(ScheduleActivity.this,"Temporarily not registere");

            }
        });

        mHosid = getIntent().getStringExtra("hosid");
        mDeptid = getIntent().getStringExtra("deptid");
        mDocid = getIntent().getStringExtra("docid");

        qrySchedule(mPage, null);
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
            case R.id.schedule_up:
                //上一页
                if (mPage == 0) {
                    ToastUtil.show(this,"First page.");
                    return;
                }
                qrySchedule(mPage, "-");
                break;
            case R.id.schedule_next:
                //下一页
                if (isLastPage) {
                    ToastUtil.show(this,"Last page.");
                    return;
                }
                qrySchedule(mPage, "+");
                break;
            case R.id.schedule_back:
                //上一页
                finish();
                break;
            case R.id.pop_locknum_success_close:
                mLockNumSuccessPop.dismiss();
                break;
            case R.id.pop_locknum_success_confirm:
                //确认挂号
                confirmOrder();
                break;
        }
    }

    private void qrySchedule(int page, final String type) {
        showProgressDialog("Querying data");
        if ("+".equals(type)) {
            page ++;
        } else if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("doctorschedule",
                new String[]{"hosid","deptid","docid","rowstart","rowcount"},
                new Object[]{mHosid,mDeptid,mDocid,page * Constant.PAGE_SIZE_10, Constant.PAGE_SIZE_10}), new Callback() {
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
                        ToastUtil.show(ScheduleActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if ("+".equals(type)) {
                    mPage ++;
                }
                if ("-".equals(type)) {
                    mPage --;
                }
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                    String message = JsonUtil.getObjectByKey("message", result);
                    String li = JsonUtil.getObjectByKey("li", message);
                    if (li != null) {
                        List<Schedule> tempList = JsonUtil.mGson.fromJson(li, new TypeToken<List<Schedule>>() {}.getType());
                        isLastPage = false;
                        if (tempList.size() < Constant.PAGE_SIZE_10) {
                            isLastPage = true;
                        }
                        mScheduleList.clear();
                        mScheduleList.addAll(tempList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                mScheduleAdapter.notifyDataSetChanged();
                            }
                        });
                        return;
                    }
                }
                isLastPage = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(ScheduleActivity.this, "No scheduling data");
                    }
                });
            }
        }));
    }

    private void locakNum() {
        showProgressDialog("Lock signal source");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("ghlock",
                new String[]{"hosid","vipcode","docid","outpdate","deptid","scheduleid","partscheduleid",
                        "certtypeno","idcard","patientname","patientsex","patientbirthday","contactphone","familyaddress"},
                new Object[]{mHosid,mVip.getVip_code(),mDocid,mOutpdate,mDeptid,mScheduleid,"",
                        "2BA",mVip.getCard_code(),mVip.getReal_name(),mVip.getSex(),mVip.getBirthday(),mVip.getMobile(),mVip.getAddress()}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(ScheduleActivity.this, e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String message = JsonUtil.getObjectByKey("message", result);
                final String ret = JsonUtil.getObjectByKey("ret", message);
                final String msg = JsonUtil.getObjectByKey("msg", message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        if ("0".equals(ret)) {
                            MyApplication.isRefreshMain = true;
                            ToastUtil.show(ScheduleActivity.this, "Locked success,In Personal Center-Register To view");
                            showLockNumSuccess(message);
                            return;
                        }
                        ToastUtil.show(ScheduleActivity.this, "Locked number failed，" + msg);
                    }
                });
            }
        }));
    }

    private void showLockNum() {
        if (mLockDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sure to lock this registeration source?？");
            builder.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    locakNum();
                }
            });
            builder.setNegativeButton("cancel", null);

            mLockDialog = builder.create();
        }
        mLockDialog.show();
    }

    private TextView mOrderIdTV,mOrderFeeTV,mLockNumTimeTV;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String mOrderId;
    private void showLockNumSuccess(String orderStr) {
        if (mLockNumSuccessPop == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.pop_locknum_success,null);
            mOrderIdTV = (TextView) view.findViewById(R.id.pop_locknum_success_orderid);
            mOrderFeeTV = (TextView) view.findViewById(R.id.pop_locknum_success_money);
            mLockNumTimeTV = (TextView) view.findViewById(R.id.pop_locknum_success_ordertime);

            view.findViewById(R.id.pop_locknum_success_close).setOnClickListener(this);
            view.findViewById(R.id.pop_locknum_success_confirm).setOnClickListener(this);

            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mLockNumSuccessPop = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mLockNumSuccessPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mLockNumSuccessPop.setOutsideTouchable(false);
        }

        mOrderId = JsonUtil.getObjectByKey("orderid",orderStr);
        mOrderIdTV.setText("Order number：" + mOrderId);
        mOrderFeeTV.setText("Registration fee：" + JsonUtil.getObjectByKey("orderfee",orderStr) + "element");
        mLockNumTimeTV.setText("Registration Time：" + mSimpleDateFormat.format(new Date(Long.parseLong(JsonUtil.getObjectByKey("ordertime",orderStr)))));

        mLockNumSuccessPop.showAtLocation(mRecyclerViewTV, Gravity.CENTER,0,0);
    }

    private void confirmOrder() {
        showProgressDialog("Confirming order");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("confirmorder",
                new String[]{"orderid"},
                new Object[]{mOrderId}), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dismissProgressDialog();
                String msg = e.getMessage();
                if (msg.startsWith("Failed")) {
                    msg = "Unable to connect to the server，Please check the network";
                }
                ToastUtil.show(ScheduleActivity.this, msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String message = JsonUtil.getObjectByKey("message", result);
                final String orderconfirmsms = JsonUtil.getObjectByKey("orderconfirmsms", message);
                final String ret = JsonUtil.getObjectByKey("ret", message);
                final String msg = JsonUtil.getObjectByKey("msg", message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        if ("0".equals(ret)) {
                            mLockNumSuccessPop.dismiss();
                            showConfirmMsg("Confirmed：" + orderconfirmsms);
                        } else {
                            ToastUtil.show(ScheduleActivity.this, "Failed to confirm：" + msg);
                        }
                    }
                });
            }
        }));
    }

    private AlertDialog.Builder mshowConfirmMsgDialog;
    private void showConfirmMsg(String msg) {
        if (mshowConfirmMsgDialog == null) {
            mshowConfirmMsgDialog = new AlertDialog.Builder(this);
            mshowConfirmMsgDialog.setTitle("Prompt");
            mshowConfirmMsgDialog.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        mshowConfirmMsgDialog.setMessage(msg);
        mshowConfirmMsgDialog.show();
    }
}