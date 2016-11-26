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
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.QuestionHistoryAdapter;
import com.ningkangyuan.bean.QuestionHistory;
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
 * 咨询(留言)
 * Created by xuchun on 2016/8/25.
 */
public class QuestionHistoryActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "QuestionHistoryActivity";

    private static final int PAGE_SIZE = 10;

    private Button mUpBtn,mNextBtn,mBackBtn;
    private QuestionHistoryAdapter mQuestionHistoryAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private List<QuestionHistory> mQuestionList = new ArrayList<QuestionHistory>();

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
        ((TextView) findViewById(R.id.universal_checkcard_num)).setText("Check card number：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.question_history, null));

        mUpBtn = (Button) findViewById(R.id.question_history_up);
        mNextBtn = (Button) findViewById(R.id.question_history_next);
        mBackBtn = (Button) findViewById(R.id.question_history_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.question_history_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.question_history_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,4);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mQuestionHistoryAdapter = new QuestionHistoryAdapter(mQuestionList));
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
                Intent intent = new Intent(QuestionHistoryActivity.this,QuestionDetailsActivity.class);
                intent.putExtra("question",mQuestionList.get(position));
                startActivity(intent);
            }
        });

        qryQuestion(mPage, null);

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
            case R.id.question_history_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"First page.");
                    return;
                }
                showProgressDialog("Querying data..");
                qryQuestion(mPage, "-");
                break;
            case R.id.question_history_next:
                //下一页
                if (mIsLastPage) {
                    ToastUtil.show(this,"Last page.");
                    return;
                }
                showProgressDialog("Querying data..");
                qryQuestion(mPage, "+");
                break;
            case R.id.question_history_back:
                //返回
                finish();
                break;
        }
    }

    private void qryQuestion(int page, final String type) {
        if ("+".equals(type)) {
            page ++;
        }
        if ("-".equals(type)) {
            page --;
        }
        showProgressDialog("Loading data..");
        mCallList.add(OkHttpHelper.get(
                OkHttpHelper.makeJsonParams("questionlist",
                        new String[]{"vip_code", "doctor_code", "pageIndex", "pageSize"},
                        new Object[]{mVip.getVip_code(), "", page, PAGE_SIZE}), new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                ToastUtil.show(QuestionHistoryActivity.this, "onFailure：" + e.getMessage());
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
                            String questions = JsonUtil.getObjectByKey("questions", result);
                            List<QuestionHistory> tempList = JsonUtil.mGson.fromJson(questions, new TypeToken<List<QuestionHistory>>() {}.getType());
                            mIsLastPage = false;
                            if (tempList.size() < PAGE_SIZE) {
                                mIsLastPage = true;
                            }
                            mQuestionList.clear();
                            mQuestionList.addAll(tempList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    mQuestionHistoryAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            mIsLastPage = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissProgressDialog();
                                    ToastUtil.show(QuestionHistoryActivity.this, "No information");
                                }
                            });
                        }
                    }
                }));
    }
}