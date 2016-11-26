package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;
import com.ningkangyuan.bean.QuestionHistory;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class QuestionHistoryAdapter extends RecyclerView.Adapter<QuestionHistoryAdapter.MyViewHolder> {

    private List<QuestionHistory> mQuestionHistoryList;

    public QuestionHistoryAdapter(List<QuestionHistory> questionHistoryList) {
        this.mQuestionHistoryList = questionHistoryList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.question_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        QuestionHistory questionHistory = mQuestionHistoryList.get(position);
        holder.title.setText(questionHistory.getTitle());
        holder.date0.setText(questionHistory.getCreate_time());
        holder.date1.setText("" + questionHistory.getLast_reply_time());
        holder.status.setVisibility(View.GONE);
        if ("1".equals(questionHistory.getStatus())) {
            holder.status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mQuestionHistoryList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title,date0,date1;
        private LinearLayout status;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.question_item_title);
            date0 = (TextView) itemView.findViewById(R.id.question_item_date0);
            date1 = (TextView) itemView.findViewById(R.id.question_item_date1);
            status = (LinearLayout) itemView.findViewById(R.id.question_item_status);
        }
    }
}