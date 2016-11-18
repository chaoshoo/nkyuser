package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;
import com.ningkangyuan.bean.Doc;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class DocAdapter extends RecyclerView.Adapter<DocAdapter.MyViewHolder> {

    private List<Doc> mDocList;

    public DocAdapter(List<Doc> docList) {
        this.mDocList = docList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.universal_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Doc doc = mDocList.get(position);
        holder.mContentTV.setText(doc.getDocname() + "\n" + doc.getDoctitle());
    }

    @Override
    public int getItemCount() {
        return mDocList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mContentTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mContentTV = (TextView) itemView.findViewById(R.id.universal_content);
        }
    }
}
