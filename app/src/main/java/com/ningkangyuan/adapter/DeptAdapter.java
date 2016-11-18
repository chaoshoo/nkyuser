package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;
import com.ningkangyuan.bean.Hos;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class DeptAdapter extends RecyclerView.Adapter<DeptAdapter.MyViewHolder> {

    private List<Dept> mDeptList;

    public DeptAdapter(List<Dept> DeptList) {
        this.mDeptList = DeptList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.universal_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Dept dept = mDeptList.get(position);
        holder.mContentTV.setText("" + dept.getDeptname());
    }

    @Override
    public int getItemCount() {
        return mDeptList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mContentTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mContentTV = (TextView) itemView.findViewById(R.id.universal_content);
        }
    }
}
