package com.ningkangyuan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ningkangyuan.R;

/**
 * 个人中心
 * Created by xuchun on 2016/8/22.
 */
public class PersonalCenterActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal);
        init();
    }

    @Override
    protected View getView() {
        return mTextView;
    }

    @Override
    protected void init() {
        mTextView = (TextView) findViewById(R.id.universal_checkcard_num);
        mTextView.setText("检查卡号：" + mVip.getCard_code());
        ((FrameLayout) findViewById(R.id.universal_content)).addView(LayoutInflater.from(this).inflate(R.layout.personal_center, null));

        findViewById(R.id.personal_center_remote).setOnClickListener(this);
        findViewById(R.id.personal_center_registration).setOnClickListener(this);
        findViewById(R.id.personal_center_consult).setOnClickListener(this);
        findViewById(R.id.personal_center_back).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal_center_remote:
                //远程诊断
            {
                Intent intent = new Intent(this,RemoteHistoryActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.personal_center_registration:
                //挂号
            {
                Intent intent = new Intent(this,OrderActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.personal_center_consult:
                //咨询
            {
                Intent intent = new Intent(this,QuestionHistoryActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.personal_center_back:
                //返回
                finish();
                break;

        }
    }
}
