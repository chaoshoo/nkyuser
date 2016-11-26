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
        String flagStr = "Can not be registered";
        if ("1".equals(validFlag)) {
            hitStr = "Available（The hospital officially confirmed the scheduling table）";
            flagStr = "Click to registr";
        } else if ("2".equals(validFlag)) {
            hitStr = "Fully booked（Registrations fully booked）";
        } else if ("3".equals(validFlag)) {
            hitStr = "close（医生close，Reservation service stopped，对已预约的号进行close正理）";
        } else if ("4".equals(validFlag)) {
            hitStr = "Available（The hospital did not officially confirmed the scheduling，Just according to the practice production scheduling";
        } else if ("5".equals(validFlag)) {
            hitStr = "Expired（Appointment expired）";
        } else if ("6".equals(validFlag)) {
            hitStr = "No（Not to allow the appointment time schedule）";
        } else if ("7".equals(validFlag)) {
            hitStr = "Time（Available/可约并且有Time";
            flagStr = "Click to registr";
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