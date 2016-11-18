package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doc;
import com.ningkangyuan.bean.Schedule;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.MyViewHolder> {

    private List<Schedule> mScheduleList;

    public ScheduleAdapter(List<Schedule> scheduleList) {
        this.mScheduleList = scheduleList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.schedule_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Schedule schedule = mScheduleList.get(position);
        holder.mTimeTV.setText(schedule.getOutpdate() + "(" + schedule.getTimeinterval() + ")");
        holder.mOrderFeeTV.setText("¥ " + schedule.getOrderfee());
        String validFlag = schedule.getValidflag();
        String hitStr = "";
        String flagStr = "不可挂号";
        if ("1".equals(validFlag)) {
            hitStr = "可挂（医院正式确定的排班表）";
            flagStr = "点击挂号";
        } else if ("2".equals(validFlag)) {
            hitStr = "已满（已经全部挂满）";
        } else if ("3".equals(validFlag)) {
            hitStr = "停诊（医生停诊，停止预约，对已预约的号进行停诊正理）";
        } else if ("4".equals(validFlag)) {
            hitStr = "可约（医院没有正式确定排班，只是根据惯例生成的排班";
        } else if ("5".equals(validFlag)) {
            hitStr = "过期（已经过预约时间的排班）";
        } else if ("6".equals(validFlag)) {
            hitStr = "未开（还没到允许预约时间的排班）";
        } else if ("7".equals(validFlag)) {
            hitStr = "分时（可挂/可约并且有分时";
            flagStr = "点击挂号";
        }
        holder.mHintTV.setText(hitStr);
        holder.mFlagTV.setText(flagStr);
    }

    @Override
    public int getItemCount() {
        return mScheduleList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTimeTV,mOrderFeeTV,mHintTV,mFlagTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTimeTV = (TextView) itemView.findViewById(R.id.schedule_item_time);
            mOrderFeeTV = (TextView) itemView.findViewById(R.id.schedule_item_orderfee);
            mHintTV = (TextView) itemView.findViewById(R.id.schedule_item_hint);
            mFlagTV = (TextView) itemView.findViewById(R.id.schedule_item_flag);
        }
    }
}
