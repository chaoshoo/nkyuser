package com.ningkangyuan.activity;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.Constant;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.HosAdapter;
import com.ningkangyuan.bean.Hos;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.bridge.RecyclerViewBridge;
import com.open.androidtvwidget.leanback.adapter.GeneralAdapter;
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
public class HosActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "RegListActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private List<Hos> mHosList = new ArrayList<Hos>();
    private HosAdapter mHosAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;

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
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("检查卡号：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.hos, null));

        mUpBtn = (Button) findViewById(R.id.hos_up);
        mNextBtn = (Button) findViewById(R.id.hos_next);
        mBackBtn = (Button) findViewById(R.id.hos_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.hos_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.hos_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,5);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mHosAdapter = new HosAdapter(mHosList));
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
                try {
                    Intent intent = new Intent(HosActivity.this,DeptActivity.class);
                    intent.putExtra("hosid",mHosList.get(position).getHosid());
                    startActivity(intent);
                } catch (Exception e) {
                    ToastUtil.show(HosActivity.this,e.getMessage());
                }

            }
        });
        qryRegHospital(mPage, null);
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
            case R.id.hos_up:
                //上一页
                if (mPage == 0) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                qryRegHospital(mPage,"-");
                break;
            case R.id.hos_next:
                //下一页
                if (isLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                qryRegHospital(mPage,"+");
                break;
            case R.id.hos_back:
                //上一页
                finish();
                break;
        }
    }

    private void qryRegHospital(int page, final String type) {
        showProgressDialog("正在查询数据");
        if ("+".equals(type)) {
            page ++;
        } else if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("hospitalalllist",
                new String[]{"hosid","rowstart","rowcount"},
                new Object[]{"0",page * Constant.PAGE_SIZE_10, Constant.PAGE_SIZE_10}), new Callback() {
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
                        ToastUtil.show(HosActivity.this, msg);
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
                        List<Hos> tempList = JsonUtil.mGson.fromJson(li, new TypeToken<List<Hos>>() {}.getType());
                        isLastPage = false;
                        if (tempList.size() < Constant.PAGE_SIZE_10) {
                            isLastPage = true;
                        }
                        mHosList.clear();
                        mHosList.addAll(tempList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                mHosAdapter.notifyDataSetChanged();
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
                        ToastUtil.show(HosActivity.this, "暂无数据显示");
                    }
                });
            }
        }));
    }
}
