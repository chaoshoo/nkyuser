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
import com.ningkangyuan.adapter.RemoteHistorydapter;
import com.ningkangyuan.bean.RemoteHistory;
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
 * 远程咨询历史
 * Created by xuchun on 2016/8/26.
 */
public class RemoteHistoryActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {
    private static final String TAG = "RemoteListActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private RemoteHistorydapter mRemoteHistorydapter;
    private List<RemoteHistory> mRemoteHistoryList = new ArrayList<RemoteHistory>();

    private int mPage = 1;
    private boolean mIsLastPage = false;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.remote_history, null));

        mUpBtn = (Button) findViewById(R.id.remote_history_up);
        mNextBtn = (Button) findViewById(R.id.remote_history_next);
        mBackBtn = (Button) findViewById(R.id.remote_history_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.remote_history_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.remote_history_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,4);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mRemoteHistorydapter = new RemoteHistorydapter(mRemoteHistoryList));
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
                Intent intent = new Intent(RemoteHistoryActivity.this, RemoteDetailsActivity.class);
                intent.putExtra("remote", mRemoteHistoryList.get(position));
                startActivityForResult(intent, 0);
            }
        });
        qryRemote(mPage, null);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            mRemoteHistoryList.clear();
            mRemoteHistorydapter.notifyDataSetChanged();
            qryRemote(mPage, null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.remote_history_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryRemote(mPage, "-");
                break;
            case R.id.remote_history_next:
                //下一页
                if (mIsLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryRemote(mPage, "+");
                break;
            case R.id.remote_history_back:
                //返回
                finish();
                break;
        }
    }

    private void qryRemote(int page, final String type) {
        showProgressDialog("正在加载..");
        if ("+".equals(type)) {
            page ++;
        }
        if ("-".equals(type)) {
            page --;
        }
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("remotelist",
                        new String[]{"vip_code", "pageIndex", "pageSize"},
                        new Object[]{mVip.getVip_code(), page, Constant.PAGE_SIZE_8}), new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                ToastUtil.show(RemoteHistoryActivity.this, "onFailure：" + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if ("+".equals(type)) {
                            mPage++;
                        }
                        if ("-".equals(type)) {
                            mPage--;
                        }
                        String result = response.body().string();
                        LogUtil.d(TAG, "onResponse：" + result);
                        if ("1".equals(JsonUtil.getObjectByKey("code", result))) {
                            String remotes = JsonUtil.getObjectByKey("remotes", result);
                            List<RemoteHistory> tempList = JsonUtil.mGson.fromJson(remotes, new TypeToken<List<RemoteHistory>>() {
                            }.getType());
                            mIsLastPage = false;
                            if (tempList.size() < Constant.PAGE_SIZE_8) {
                                mIsLastPage = true;
                            }
                            mRemoteHistoryList.clear();
                            mRemoteHistoryList.addAll(tempList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    mRemoteHistorydapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            mIsLastPage = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    ToastUtil.show(RemoteHistoryActivity.this, "暂无信息数据");
                                }
                            });
                        }
                    }
                }));
    }
}
