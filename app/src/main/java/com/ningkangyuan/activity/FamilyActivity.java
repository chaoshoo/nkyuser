package com.ningkangyuan.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.MyApplication;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.FamilyAdapter;
import com.ningkangyuan.bean.Vip;
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
 * 成员管理
 * Created by xuchun on 2016/9/1.
 */
public class FamilyActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {
    private static final String TAG = "FamilyActivity";

    private TextView mCurrentNameTV;
    private FamilyAdapter mFamilyAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private List<Vip> mFamilyList = new ArrayList<Vip>();

    private PopupWindow mFamilySavePW;

    private int mPosition = 0;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.family, null));

        mCurrentNameTV = (TextView) findViewById(R.id.family_current_name);
        String userName = mVip.getReal_name();
        if (TextUtils.isEmpty(userName)) {
            userName = mVip.getMobile();
        }
        mCurrentNameTV.setText("Current member：" + userName);
        findViewById(R.id.family_back).setOnClickListener(this);
        findViewById(R.id.family_back).setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.family_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.family_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,1);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.HORIZONTAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mFamilyAdapter = new FamilyAdapter(mFamilyList));
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
                mPosition = position;
                if (position > 1) {
                    showItemClickDialog();
                } else {
                    if (position == 0) {
                        showFamilyChildSave();
                    }
                }
            }
        });

        clearList();
        qryFamilyChild();
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
            case R.id.family_back:
                //返回
                finish();
                break;
            case R.id.family_save_close:
                if (mFamilySavePW != null) {
                    mFamilySavePW.dismiss();
                }
                break;
            case R.id.family_save_confirm:
                //绑定
                saveFamilyChild();
                break;
        }
    }

    private void clearList() {
        mFamilyList.clear();

        Vip addVip = new Vip();
        addVip.setReal_name("Bind member");
        mFamilyList.add(addVip);

        addVip = new Vip();
        addVip.setReal_name("WeChat sweep me");
        mFamilyList.add(addVip);

        mFamilyAdapter.notifyDataSetChanged();
    }

    private void qryFamilyChild() {
        showProgressDialog("Searching member data..");
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("vipfamilylist",
                        new String[]{"vip_code"},
                        new Object[]{mVip.getVip_code()}), new Callback() {
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
                                ToastUtil.show(FamilyActivity.this, msg);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        LogUtil.d(TAG, "onResponse：" + result);
                        if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                            String familys = JsonUtil.getObjectByKey("familys", result);
                            List<Vip> tempList = JsonUtil.mGson.fromJson(familys, new TypeToken<List<Vip>>() {
                            }.getType());

                            mFamilyList.clear();

                            //构造一个Vip,作为第一个使用
                            Vip addVip = new Vip();
                            addVip.setReal_name("Bind member");
                            mFamilyList.add(addVip);

                            addVip = new Vip();
                            addVip.setReal_name("WeChat sweep me");
                            mFamilyList.add(addVip);
                            mFamilyList.addAll(tempList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    mFamilyAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    clearList();
                                    dismissProgressDialog();
                                }
                            });
                        }
                    }
                }));
    }

    private EditText mFamilyAccountET,mFamilyPassET;
    private void showFamilyChildSave() {
        if (mFamilySavePW == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.family_save,null);
            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            mFamilyAccountET = (EditText) view.findViewById(R.id.family_save_card);
            mFamilyPassET = (EditText) view.findViewById(R.id.family_save_pass);
            view.findViewById(R.id.family_save_close).setOnClickListener(this);
            view.findViewById(R.id.family_save_confirm).setOnClickListener(this);

            mFamilySavePW = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mFamilySavePW.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mFamilySavePW.setOutsideTouchable(false);
        }
        mFamilyAccountET.requestFocus();
        mFamilySavePW.showAtLocation(mCurrentNameTV, Gravity.CENTER,0,0);
    }

    private void saveFamilyChild() {
        String card = mFamilyAccountET.getText().toString().trim();
        if (TextUtils.isEmpty(card)) {
            ToastUtil.show(this,"Please enter your ID number.");
            return;
        }
        String pass = mFamilyPassET.getText().toString().trim();
        if (TextUtils.isEmpty(card)) {
            ToastUtil.show(this, "Please input a password");
            return;
        }
        showProgressDialog("Bounding..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("savevipfamily",
                new String[]{"vip_code", "family_account", "family_pwd"},
                new Object[]{mVip.getVip_code(), card, pass}), new Callback() {
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
                        ToastUtil.show(FamilyActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String code = JsonUtil.getObjectByKey("code", result);
                final String message = JsonUtil.getObjectByKey("message", result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(FamilyActivity.this, message);
                        if ("1".equals(code)) {
                            if (mFamilySavePW != null) {
                                mFamilySavePW.dismiss();
                            }
                            qryFamilyChild();
                        }
                    }
                });
            }
        }));
    }

    //切换成员
    private void switchChild() {
        mVip = mFamilyList.get(mPosition);
        MyApplication.isRefreshMain = true;
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("Check card number：" + mVip.getCard_code());
        String userName = mVip.getReal_name();
        if (TextUtils.isEmpty(userName)) {
            userName = mVip.getMobile();
        }
        mCurrentNameTV.setText(userName);
        getSharedPreferences("login", 0).edit().putString("vip", JsonUtil.mGson.toJson(mVip)).commit();
        clearList();
        ToastUtil.show(FamilyActivity.this, "Members switched");
        qryFamilyChild();
    }

    private void unBindChild() {
        showProgressDialog("Removing binding..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("deletebind",
                new String[]{"vip_code", "vip_card", "bind_account"},
                new Object[]{mVip.getVip_code(), mVip.getCard_code(), mFamilyList.get(mPosition).getCard_code()}), new Callback() {
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
                        ToastUtil.show(FamilyActivity.this, msg);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                LogUtil.d(TAG, "onResponse：" + result);
                final String code = JsonUtil.getObjectByKey("code", result);
                final String message = JsonUtil.getObjectByKey("message", result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(FamilyActivity.this, message);
                        if ("1".equals(code)) {
                            qryFamilyChild();
                        }
                    }
                });
            }
        }));
    }

    private Dialog mItemClickDialog;
    private void showItemClickDialog() {
        if (mItemClickDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Prompt");
            builder.setMessage("Please select operation");
            builder.setPositiveButton("Click the wrong place", null);
            builder.setNeutralButton("Switch to this member", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchChild();
                }
            });
            builder.setNegativeButton("Bind this member", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    unBindChild();
                }
            });
            mItemClickDialog = builder.create();
        }
        mItemClickDialog.show();
    }
}