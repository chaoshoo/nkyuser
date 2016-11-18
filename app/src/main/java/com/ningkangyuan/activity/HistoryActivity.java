package com.ningkangyuan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ningkangyuan.Constant;
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.DeptAdapter;
import com.ningkangyuan.adapter.HistoryAdapter;
import com.ningkangyuan.bean.Vip;
import com.ningkangyuan.kpi.Measure;
import com.ningkangyuan.utils.JsonUtil;
import com.ningkangyuan.utils.LogUtil;
import com.ningkangyuan.utils.ScreenUtil;
import com.ningkangyuan.utils.ToastUtil;
import com.open.androidtvwidget.bridge.EffectNoDrawBridge;
import com.open.androidtvwidget.leanback.recycle.GridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.view.MainUpView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 历史检测
 * Created by xuchun on 2016/8/29.
 */
public class HistoryActivity extends BaseActivity implements View.OnFocusChangeListener {

    private static final String TAG = "HistoryActivity";

    private HistoryAdapter mHistoryAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private WebView mWebView;

    private int mWebWidth;
    private int mWebHeight;

    private boolean mIsFristLoad = true;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.history, null));

        findViewById(R.id.history_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.history_back).setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.history_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.history_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,1);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.VERTICAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mHistoryAdapter = new HistoryAdapter(Constant.NORM));
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
                JSONObject norm = Constant.NORM.get(position);
                try {
                    String inspect_code = norm.getString("inspect_code");
                    if ("C06".equals(inspect_code)) {
                        loadNiao();
                        return;
                    }
                    load(inspect_code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mWebView = (WebView) findViewById(R.id.history_web);
        mWebView.requestFocusFromTouch();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtil.d(TAG, "url：" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showProgressDialog("正在加载..");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                dismissProgressDialog();
            }
        });

        mWebView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mIsFristLoad) {
                    mIsFristLoad = false;
                    mWebWidth = mWebView.getWidth();
                    mWebHeight = mWebView.getHeight();
                    load(Measure.XueYa.INSPECT_CODE);
                }
            }
        });

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mMainUpView.setFocusView(v, 1.0f);
        } else {
            mMainUpView.setUnFocusView(v);
        }
    }

    private void load(String inspect_code) {
        String url = "http://114.55.228.245:83/nkyplatform/vipInspectData/chartall/" + mVip.getCard_code() + "/" + inspect_code + "/ALL/" + mWebWidth + "-" + mWebHeight + "/0.html";
        LogUtil.d(TAG,"url：" + url);
        mWebView.loadUrl(url);
    }

    private void loadNiao() {
        String url = "http://114.55.228.245:83/nkyplatform/vipInspectData/getDatagrid/" + mVip.getCard_code() + "/C06/ALL/" + mWebWidth + "-" + mWebHeight + "/0.html";
        LogUtil.d(TAG,"url：" + url);
        mWebView.loadUrl(url);
    }
}
