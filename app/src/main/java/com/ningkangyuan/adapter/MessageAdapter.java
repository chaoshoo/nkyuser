package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Hos;
import com.ningkangyuan.bean.Message;

import java.util.List;

/**
 * 消息列表
 * Created by xuchun on 2016/9/21.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> mMessageList;

    public MessageAdapter(List<Message> messageList) {
        this.mMessageList = messageList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.message_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        holder.title.setText(message.getTitle());
        holder.content.setText("  " + message.getContent());
        holder.time.setText(message.getCreate_time());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title,content,time;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.message_item_title);
            content = (TextView) itemView.findViewById(R.id.message_item_content);
            time = (TextView) itemView.findViewById(R.id.message_item_time);
        }
    }
}