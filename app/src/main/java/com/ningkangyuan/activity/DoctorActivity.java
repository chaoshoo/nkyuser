package com.ningkangyuan.activity;

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
import com.ningkangyuan.R;
import com.ningkangyuan.adapter.DoctorAdapter;
import com.ningkangyuan.bean.Doctor;
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
 * Created by xuchun on 2016/8/22.
 */
public class DoctorActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener {

    private static final String TAG = "DoctorActivity";

    private static final int PAGE_SIZE = 6;

    private Button mUpBtn,mNextBtn,mBackBtn;
    private DoctorAdapter mDoctorAdapter;
    private RecyclerViewTV mRecyclerViewTV;
    private MainUpView mMainUpView;
    private List<Doctor> mDoctorList = new ArrayList<Doctor>();

    private PopupWindow mDoctorSelectPW;

    private int mPage = 1;
    private boolean mIsLastPage = false;
    private String office_code;
    private String hospital_code;

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
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.doctor, null));

        mUpBtn = (Button) findViewById(R.id.doctor_up);
        mNextBtn = (Button) findViewById(R.id.doctor_next);
        mBackBtn = (Button) findViewById(R.id.doctor_back);
        mUpBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mUpBtn.setOnFocusChangeListener(this);
        mNextBtn.setOnFocusChangeListener(this);
        mBackBtn.setOnFocusChangeListener(this);

        mMainUpView = (MainUpView) findViewById(R.id.doctor_upview);
        mMainUpView.setEffectBridge(new EffectNoDrawBridge());
        EffectNoDrawBridge effectNoDrawBridge = (EffectNoDrawBridge) mMainUpView.getEffectBridge();
        effectNoDrawBridge.setTranDurAnimTime(200);
        mMainUpView.setUpRectResource(R.drawable.test_rectangle); // 设置移动边框的图片.
        mMainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.

        mRecyclerViewTV = (RecyclerViewTV) findViewById(R.id.doctor_rv);
        GridLayoutManagerTV gridLayoutManagerTV = new GridLayoutManagerTV(this,1);
        gridLayoutManagerTV.setOrientation(GridLayoutManagerTV.HORIZONTAL);
        mRecyclerViewTV.setLayoutManager(gridLayoutManagerTV);
        mRecyclerViewTV.setAdapter(mDoctorAdapter = new DoctorAdapter(mDoctorList));
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
                showDoctorSelect(position);
            }
        });

        office_code = getIntent().getStringExtra("office_code");
        if (office_code == null) {
            office_code = "";
        }
        hospital_code = getIntent().getStringExtra("hospital_code");
        if (hospital_code == null) {
            hospital_code = "";
        }
        //得到医生数据
        showProgressDialog("正在查询医生数据..");
        qryDoctor(mPage,null);
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
            case R.id.doctor_select_close:
                if (mDoctorSelectPW != null) {
                    mDoctorSelectPW.dismiss();
                }
                break;
            case R.id.doctor_select_remote:
            {
                //远程咨询诊断
                Integer postion = (Integer) v.getTag();
                Intent intent = new Intent(this,RemoteSaveActivity.class);
                intent.putExtra("doctor",mDoctorList.get(postion));
                startActivity(intent);
                mDoctorSelectPW.dismiss();
            }
                break;
            case R.id.doctor_select_question:
                //留言
                Integer postion = (Integer) v.getTag();
                Intent intent = new Intent(this,QuestionSaveActivity.class);
                intent.putExtra("doctor",mDoctorList.get(postion));
                startActivity(intent);
                mDoctorSelectPW.dismiss();
                break;

            case R.id.doctor_up:
                //上一页
                if (mPage == 1) {
                    ToastUtil.show(this,"已经是第一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryDoctor(mPage,"-");
                break;
            case R.id.doctor_next:
                //下一页
                if (mIsLastPage) {
                    ToastUtil.show(this,"已经是最后一页了");
                    return;
                }
                showProgressDialog("正在查询数据..");
                qryDoctor(mPage,"+");
                break;
            case R.id.doctor_back:
                //返回
                finish();
                break;
        }
    }

    private void qryDoctor(int page, final String type) {
        if ("+".equals(type)) {
            page ++;
        }
        if ("-".equals(type)) {
            page --;
        }
        showProgressDialog("正在获取医生信息..");
        mCallList.add(OkHttpHelper.get(OkHttpHelper.makeJsonParams("doctors",
                new String[]{"code","name","pageIndex","pageSize","office_code","hospital_code"},
                new Object[]{"","",page,PAGE_SIZE,office_code,hospital_code}), new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        ToastUtil.show(DoctorActivity.this, "onFailure：" + e.getMessage());
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
                LogUtil.d(TAG,"onResponse：" + result);
                if ("1".equals(JsonUtil.getObjectByKey("code",result))) {
                    String doctors = JsonUtil.getObjectByKey("doctors",result);
                    List<Doctor> tempList = JsonUtil.mGson.fromJson(doctors,new TypeToken<List<Doctor>>() {}.getType());
                    mIsLastPage = false;
                    if (tempList.size() < PAGE_SIZE) {
                        mIsLastPage = true;
                    }
                    mDoctorList.clear();
                    mDoctorList.addAll(tempList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            mDoctorAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    mIsLastPage = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            ToastUtil.show(DoctorActivity.this, "暂无医生信息");
                        }
                    });
                }
            }
        }));
    }

    private Button remoteBTN,questionBTN;
    private void showDoctorSelect(int position) {
        if (mDoctorSelectPW == null) {

            View view = LayoutInflater.from(this).inflate(R.layout.doctor_select,null);
            int width = ScreenUtil.getWidth(this) * 3 /4;
//            int height = ScreenUtil.getHeight(this) * 3 / 4;
            view.findViewById(R.id.doctor_select_close).setOnClickListener(this);
            remoteBTN = (Button) view.findViewById(R.id.doctor_select_remote);
            questionBTN = (Button) view.findViewById(R.id.doctor_select_question);
            remoteBTN.setOnClickListener(this);
            questionBTN.setOnClickListener(this);

            mDoctorSelectPW = new PopupWindow(view,width, WindowManager.LayoutParams.WRAP_CONTENT,true);
            mDoctorSelectPW.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mDoctorSelectPW.setOutsideTouchable(false);
        }
        remoteBTN.setTag(position);
        questionBTN.setTag(position);
        remoteBTN.requestFocus();
        mDoctorSelectPW.showAtLocation(mUpBtn, Gravity.CENTER,0,0);
    }
}
