package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.Constant;
import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<JSONObject> mNormList;

    public HistoryAdapter(List<JSONObject> normList) {
        this.mNormList = normList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.history_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        JSONObject norm = Constant.NORM.get(position);
        try {
            holder.mContentTV.setText("" + norm.getString("inspect_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mNormList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mContentTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mContentTV = (TextView) itemView.findViewById(R.id.history_item_content);
        }
    }
}
