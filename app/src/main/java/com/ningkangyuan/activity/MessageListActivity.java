package com.ningkangyuan.activity;

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
import com.ningkangyuan.adapter.MessageAdapter;
import com.ningkangyuan.bean.Message;
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
 * 消息列表
 * Created by xuchun on 2016/8/24.
 */
public class MessageListActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {
    private static final String TAG = "DoctorActivity";

    private Button mUpBtn,mNextBtn,mBackBtn;
    private MessageAdapter mMessageAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private List<Message> mMessageList = new ArrayList<Message>();

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.message, null));

        mUpBtn = (Button) findViewById(R.id.message_up);
        mNextBtn = (Button) findViewById(R.id.message_next);
        mBackBtn = (Button) findViewById(R.id.message_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.message_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.message_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,4);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mMessageAdapter = new MessageAdapter(mMessageList));
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


            }
        });

        qryMessage(mPage,null);
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
            case R.id.message_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryMessage(mPage, "-");
                break;
            case R.id.message_next:
                //下一页
                if (mIsLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryMessage(mPage, "+");
                break;
            case R.id.message_back:
                //返回
                finish();
                break;
        }
    }

    private void qryMessage(int page, final String type) {
        if ("+".equals(type)) {
            page ++;
        }
        if ("-".equals(type)) {
            page --;
        }
        showProgressDialog("加载数据中..");
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("messagelist",new String[]{"msg_type","vip_id","pageIndex","pageSize"},
                        new Object[]{"",mVip.getId(),page, Constant.PAGE_SIZE_8}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(MessageListActivity.this, "onFailure：" + e.getMessage());
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
                    String doctors = JsonUtil.getObjectByKey("messages", result);
                    List<Message> tempList = JsonUtil.mGson.fromJson(doctors, new TypeToken<List<Message>>() {
                    }.getType());
                    mIsLastPage = false;
                    if (tempList.size() <Constant.PAGE_SIZE_8) {
                        mIsLastPage = true;
                    }
                    mMessageList.clear();
                    mMessageList.addAll(tempList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    mIsLastPage = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            ToastUtil.show(MessageListActivity.this, "暂无信息数据");
                        }
                    });
                }
            }
        }));
    }
}
