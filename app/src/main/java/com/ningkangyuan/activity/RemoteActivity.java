package com.ningkangyuan.activity;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.ningkangyuan.Constant;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.RemoteAdapter;
import com.ningkangyuan.bean.Hospital;
import com.ningkangyuan.bean.Office;
import com.ningkangyuan.okhttp.OkHttpHelper;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
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
 * Created by xuchun on 2016/8/22.
 */
public class RemoteActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "DepartmentActivity";

    private Button mOffcieBtn,mHospital,mUpBtn,mNextBtn,mBackBtn;
    private RemoteAdapter mRemoteAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private ArrayList<Office> mOfficeList = new ArrayList<Office>();
    private ArrayList<Hospital> mHospitalsList = new ArrayList<Hospital>();

    private String mDataType = "offices";
    private int mPage = 1;
    private boolean isLastPage = false;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.remote, null));

        mOffcieBtn = (Button) findViewById(R.id.remote_office);
        mHospital = (Button) findViewById(R.id.remote_hospital);
        mUpBtn = (Button) findViewById(R.id.remote_up);
        mNextBtn = (Button) findViewById(R.id.remote_next);
        mBackBtn = (Button) findViewById(R.id.remote_back);

        mOffcieBtn.setOnClickListener(this);
        mHospital.setOnClickListener(this);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);

        mOffcieBtn.setOnFocusChangeListener(this);
        mHospital.setOnFocusChangeListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.remote_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRemoteAdapter = new RemoteAdapter();
        mRemoteAdapter.setData(mOfficeList,mHospitalsList,mDataType);
        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.remote_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,5);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mRemoteAdapter);
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
                Intent intent = new Intent(RemoteActivity.this,DoctorActivity.class);
                if (mDataType.equals("offices")) {
                    intent.putExtra("office_code", mOfficeList.get(position).getCode());
                } else if (mDataType.equals("hospitals")) {
                    intent.putExtra("hospital_code",mHospitalsList.get(position).getCode());
                }
                startActivity(intent);
            }
        });

        showProgressDialog("正在查询数据..");
        qryOffices(mPage, null);
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
            case R.id.remote_office:
                //科室
                mDataType = "offices";
                mPage = 1;
                showProgressDialog("正在查询数据..");
                qryOffices(mPage,null);
                break;
            case R.id.remote_hospital:
                //医院
                mDataType = "hospitals";
                mPage = 1;
                showProgressDialog("正在查询数据..");
                qryOffices(mPage,null);
                break;
            case R.id.remote_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryOffices(mPage,"-");
                break;
            case R.id.remote_next:
                //下一页
                if (isLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryOffices(mPage,"+");
                break;
            case R.id.remote_back:
                //返回
                finish();
                break;
        }
    }

    private void qryOffices(int page, final String type) {
        if ("+".equals(type)) {
            page ++;
        }
        if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams(mDataType,
                new String[]{"code","pageIndex","pageSize"},
                new Object[]{"",page, Constant.PAGE_SIZE_10}), new Callback() {
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
                        ToastUtil.show(RemoteActivity.this, msg);
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
                    if (mDataType.equals("offices")) {
                        String offices = JsonUtil.getObjectByKey("offices", result);
                        List<Office> tempList = JsonUtil.mGson.fromJson(offices, new TypeToken<List<Office>>() {}.getType());
                        isLastPage = false;
                        if (tempList.size() < Constant.PAGE_SIZE_10) {
                            isLastPage = true;
                        }
                        mOfficeList.clear();
                        mOfficeList.addAll(tempList);
                    } else if(mDataType.equals("hospitals")) {
                        String hospitals = JsonUtil.getObjectByKey("hospitals", result);
                        List<Hospital> tempList = JsonUtil.mGson.fromJson(hospitals, new TypeToken<List<Hospital>>() {}.getType());
                        isLastPage = false;
                        if (tempList.size() < Constant.PAGE_SIZE_10) {
                            isLastPage = true;
                        }
                        mHospitalsList.clear();
                        mHospitalsList.addAll(tempList);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            mRemoteAdapter.setData(mOfficeList,mHospitalsList,mDataType);
                            mRemoteAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    isLastPage = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            ToastUtil.show(RemoteActivity.this, "暂无数据显示");
                        }
                    });
                }
            }
        }));
    }
}
