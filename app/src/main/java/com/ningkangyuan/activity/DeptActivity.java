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
import com.ningkangyuan.adapter.DeptAdapter;
import com.ningkangyuan.bean.Dept;
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
 * 挂号科室
 * Created by xuchun on 2016/9/1.
 */
public class DeptActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "DeptActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private List<Dept> mDeptList = new ArrayList<Dept>();
    private DeptAdapter mDeptAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;

    private String mHosid;
    private boolean isLastPage = false;
    private int mPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        try {
            init();
        } catch (Exception e) {
            ToastUtil.show(this,e.getMessage());
        }

    }

    @Override
    protected View getView() {
        return mRecyclerViewTV;
    }

    @Override
    protected void init() {
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("检查卡号：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.dept, null));

        mUpBtn = (Button) findViewById(R.id.dept_up);
        mNextBtn = (Button) findViewById(R.id.dept_next);
        mBackBtn = (Button) findViewById(R.id.dept_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.dept_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.dept_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,5);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mDeptAdapter = new DeptAdapter(mDeptList));
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
                Intent intent = new Intent(DeptActivity.this,DocActivity.class);
                intent.putExtra("hosid",mHosid);
                intent.putExtra("deptid",mDeptList.get(position).getDeptid());
                startActivity(intent);
            }
        });

        mHosid = getIntent().getStringExtra("hosid");

        qryDept(mPage, null);
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
            case R.id.dept_up:
                //上一页
                if (mPage == 0) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                qryDept(mPage, "-");
                break;
            case R.id.dept_next:
                //下一页
                if (isLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                qryDept(mPage, "+");
                break;
            case R.id.dept_back:
                //上一页
                finish();
                break;
        }
    }

    private void qryDept(int page, final String type) {
        showProgressDialog("正在查询数据");
        if ("+".equals(type)) {
            page ++;
        } else if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("deptalllist",
                new String[]{"hosid","rowstart","rowcount"},
                new Object[]{mHosid,page * Constant.PAGE_SIZE_10,Constant.PAGE_SIZE_10}), new Callback() {
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
                        ToastUtil.show(DeptActivity.this, msg);
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
                        List<Dept> tempList = JsonUtil.mGson.fromJson(li, new TypeToken<List<Dept>>() {}.getType());
                        isLastPage = false;
                        if (tempList.size() < Constant.PAGE_SIZE_10) {
                            isLastPage = true;
                        }
                        mDeptList.clear();
                        mDeptList.addAll(tempList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                mDeptAdapter.notifyDataSetChanged();
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
                        ToastUtil.show(DeptActivity.this, "暂无数据显示");
                    }
                });
            }
        }));
    }
}
