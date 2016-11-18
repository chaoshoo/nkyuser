package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;
import com.ningkangyuan.bean.RemoteHistory;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class RemoteHistorydapter extends RecyclerView.Adapter<RemoteHistorydapter.MyViewHolder> {

    private List<RemoteHistory> mRemoteHistoryList;

    public RemoteHistorydapter(List<RemoteHistory> remoteHistoryList) {
        this.mRemoteHistoryList = remoteHistoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.remote_history_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RemoteHistory remoteHistory = mRemoteHistoryList.get(position);
        holder.doctor.setText("预约医生：" + remoteHistory.getName() + "\n预约时间：" + remoteHistory.getOrder_time());
        holder.remark.setText("预约备注：\n" + remoteHistory.getRemark());
        String isZd = remoteHistory.getIszd();
        if ("1".equals(isZd)) {
            isZd = "已应答";
        } else if ("2".equals(isZd)) {
            isZd = "已拒绝";
        } else {
            isZd = "未处理";
        }
        String isDeal = remoteHistory.getIsdeal();
        if ("1".equals(isDeal)) {
            isDeal = "已视频";
        } else if ("2".equals(isDeal)) {
            isDeal = "已拒绝";
        } else {
            isDeal = "未处理";
        }
        holder.status.setText("应诊状态：" + isZd + "\n视频状态：" + isDeal);
    }

    @Override
    public int getItemCount() {
        return mRemoteHistoryList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView doctor,remark,status;

        public MyViewHolder(View itemView) {
            super(itemView);
            doctor = (TextView) itemView.findViewById(R.id.remote_item_doctor);
            remark = (TextView) itemView.findViewById(R.id.remote_item_remark);
            status = (TextView) itemView.findViewById(R.id.remote_item_status);
        }
    }
}
