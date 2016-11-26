package com.ningkangyuan.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.Constant;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.HosAdapter;
import com.ningkangyuan.adapter.OrderAdapter;
import com.ningkangyuan.bean.Hos;
import com.ningkangyuan.bean.Order;
import com.ningkangyuan.image.ImageLoaderHelper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/1.
 */
public class OrderActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "OrderActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private List<Order> mOrderList = new ArrayList<Order>();
    private OrderAdapter mOrderAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;

    private boolean isLastPage = false;
    private int mPage = 1;

    private Order mOrder;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.order, null));

        mUpBtn = (Button) findViewById(R.id.order_up);
        mNextBtn = (Button) findViewById(R.id.order_next);
        mBackBtn = (Button) findViewById(R.id.order_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.order_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.order_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,4);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mOrderAdapter = new OrderAdapter(mOrderList));
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
                //显示确认订单、取消订单
                mOrder = mOrderList.get(position);
                String status = mOrder.getStatus();
                if ("1".equals(status) || "2".equals(status) || "3".equals(status)) {
                    //操作订单
                    showOrderOperationPop();
                    return;
                }
                ToastUtil.show(OrderActivity.this,"The order is not available");
            }
        });
        qryOrder(mPage, null);
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
            case R.id.order_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"First page.");
                    return;
                }
                qryOrder(mPage, "-");
                break;
            case R.id.order_next:
                //下一页
                if (isLastPage) {
                    ToastUtil.show(this,"Last page.");
                    return;
                }
                qryOrder(mPage, "+");
                break;
            case R.id.order_back:
                //上一页
                finish();
                break;
            case R.id.pop_order_operation_close:
                mOrderOperationPop.dismiss();
                break;
            case R.id.pop_order_operation_yes:
                String status = mOrder.getStatus();
                if ("1".equals(status)) {
                    //确认订单
                    showConfirmHintDialog();
                } else if ("2".equals(status)) {
                    //支付订单
                    showPayPop(mOrder.getOrderid());
                }
                break;
            case R.id.pop_order_operation_cancel:
                mOrderOperationPop.dismiss();
                showCancelPop();
                break;
        }
    }

    private void qryOrder(int page, final String type) {
        showProgressDialog("Querying data");
        if ("+".equals(type)) {
            page ++;
        } else if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("getghorderlst",
                new String[]{"orderId","status","hosid","vipcode","docid","deptid","patientname","pageIndex","pageSize"},
                new Object[]{"","","",mVip.getVip_code(),"","","",page,Constant.PAGE_SIZE_8}), new Callback() {
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
                        ToastUtil.show(OrderActivity.this, msg);
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
                    String orders = JsonUtil.getObjectByKey("orders", result);
                    List<Order> tempList = JsonUtil.mGson.fromJson(orders, new TypeToken<List<Order>>() {}.getType());
                    isLastPage = false;
                    if (tempList.size() < Constant.PAGE_SIZE_8) {
                        isLastPage = true;
                    }
                    mOrderList.clear();
                    mOrderList.addAll(tempList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            mOrderAdapter.notifyDataSetChanged();
                        }
                    });
                    return;

                }
                isLastPage = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(OrderActivity.this, "No data show");
                    }
                });
            }
        }));
    }

    private PopupWindow mOrderOperationPop;
    private TextView mOrderIdTV,mOrderFeeTV,mLockNumTimeTV,mPayStatus;
    private Button mYesBtn;
    private void showOrderOperationPop() {
        if (mOrderOperationPop == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.pop_order_operation,null);
            mOrderIdTV = (TextView) view.findViewById(R.id.pop_order_operation_orderid);
            mOrderFeeTV = (TextView) view.findViewById(R.id.pop_order_operation_money);
//            mLockNumTimeTV = (TextView) view.findViewById(R.id.pop_order_operation_ordertime);
            mPayStatus = (TextView) view.findViewById(R.id.pop_order_operation_paystatus);

            view.findViewById(R.id.pop_order_operation_close).setOnClickListener(this);
            mYesBtn = (Button) view.findViewById(R.id.pop_order_operation_yes);
            mYesBtn.setOnClickListener(this);
            view.findViewById(R.id.pop_order_operation_cancel).setOnClickListener(this);

            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mOrderOperationPop = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mOrderOperationPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mOrderOperationPop.setOutsideTouchable(false);
        }

        mYesBtn.setVisibility(View.VISIBLE);
        mOrderIdTV.setText("Order number：" + mOrder.getOrderid());
        mOrderFeeTV.setText("Registration fee：" + mOrder.getOrderfee() + "element");
        String status = mOrder.getStatus();
        if ("1".equals(status)) {
            status = "Locked success";
            mYesBtn.setText("Confirm order");
        } else if ("2".equals(status)) {
            status = "Confirmed";
            mYesBtn.setText("To pay");
        } else if ("3".equals(status)) {
            status = "Payed";
            mYesBtn.setVisibility(View.GONE);
        }
        mPayStatus.setText("Payment status：" + status);
        mOrderOperationPop.showAtLocation(mRecyclerViewTV,Gravity.CENTER,0,0);
    }

    private Dialog mConfirmHintDialog;
    private void showConfirmHintDialog() {
        if (mConfirmHintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure you confirm the order？");
            builder.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmOrder();
                }
            });
            builder.setNegativeButton("cancel", null);
            mConfirmHintDialog = builder.create();
        }
        mConfirmHintDialog.show();
    }

    private void confirmOrder() {
        showProgressDialog("Confirming order");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("confirmorder",
                new String[]{"orderid"},
                new Object[]{mOrder.getOrderid()}), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dismissProgressDialog();
                String msg = e.getMessage();
                if (msg.startsWith("Failed")) {
                    msg = "Unable to connect to the server，Please check the network";
                }
                ToastUtil.show(OrderActivity.this, msg);
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
                            mOrderOperationPop.dismiss();
                            showConfirmMsg("Confirmed：" + orderconfirmsms);
                        } else {
                            ToastUtil.show(OrderActivity.this, "Failed to confirm：" + msg);
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
                    qryOrder(mPage, null);
                }
            });
        }
        mshowConfirmMsgDialog.setMessage(msg);
        mshowConfirmMsgDialog.show();
    }

    private PopupWindow mCancelPop;
    private EditText mCancelReasonET;
    private void showCancelPop() {
        if (mCancelPop == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.pop_order_cancel,null);
            view.findViewById(R.id.pop_order_cancel_close).setOnClickListener(cancelListener);
            view.findViewById(R.id.pop_order_cancel_confirm).setOnClickListener(cancelListener);
            mCancelReasonET = (EditText) view.findViewById(R.id.pop_order_cancel_reason);

            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mCancelPop = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mCancelPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mCancelPop.setOutsideTouchable(false);
        }
        mCancelPop.showAtLocation(mRecyclerViewTV, Gravity.CENTER,0,0);
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pop_order_cancel_close:
                    mCancelPop.dismiss();
                    showOrderOperationPop();
                    break;
                case R.id.pop_order_cancel_confirm:
                    showCancelHintDialog();
                    break;
            }
        }
    };

    private Dialog mCancelHintDialog;
    private void showCancelHintDialog() {
        if (mCancelHintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure cancel order？");
            builder.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelOrder();
                }
            });
            builder.setNegativeButton("cancel", null);
            mCancelHintDialog = builder.create();
        }
        mCancelHintDialog.show();
    }

    private void cancelOrder() {
        String reason = mCancelReasonET.getText().toString().trim();
        if (TextUtils.isEmpty(reason)) {
            ToastUtil.show(this,"Please enter the reason");
            return;
        }
        showProgressDialog("Cancelling the order");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("cancelorder",
                new String[]{"cancelreason","operator","orderid"},
                new Object[]{reason,mVip.getCard_code(),mOrder.getOrderid()}), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dismissProgressDialog();
                String msg = e.getMessage();
                if (msg.startsWith("Failed"))  {
                    msg = "Unable to connect to the server，Please check the network";
                }
                ToastUtil.show(OrderActivity.this, msg);
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
                            mCancelPop.dismiss();
                            ToastUtil.show(OrderActivity.this,msg);
                            qryOrder(mPage,null);
                        } else {
                            ToastUtil.show(OrderActivity.this,"Order cancellation failed：" + msg);
                        }
                    }
                });
            }
        }));
    }


    private PopupWindow mPayPop;
    private ImageView mPayEwmIV;
    private void showPayPop(String orderId) {
        if (mPayPop == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.pop_order_pay,null);
            mPayEwmIV = (ImageView) view.findViewById(R.id.pop_order_pay_erweima);
            view.findViewById(R.id.pop_order_pay_yes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPayPop.dismiss();
                    mOrderOperationPop.dismiss();
                    qryOrder(mPage,null);
                }
            });
            view.findViewById(R.id.pop_order_pay_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPayPop.dismiss();
                }
            });


            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mPayPop = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mPayPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPayPop.setOutsideTouchable(false);
        }
        ImageLoaderHelper.getInstance().loader("http://114.55.228.245:84/nkyapi/pay/getwxqrcode.do?orderid=" + orderId,
                mPayEwmIV,
                null);
        mPayPop.showAtLocation(mRecyclerViewTV, Gravity.CENTER,0,0);
    }
}