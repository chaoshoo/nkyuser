package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Dept;
import com.ningkangyuan.bean.Hospital;
import com.ningkangyuan.bean.Office;

import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.MyViewHolder> {

    //默认科室
    private String mType = "offices";

    private List<Office> mOfficeList;
    private List<Hospital> mHospitalList;

    public void setData(List<Office> officeList,List<Hospital> hospitalList,String type) {
        this.mOfficeList = officeList;
        this.mHospitalList = hospitalList;
        this.mType = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.universal_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = "";
        if (mType.equals("offices")) {
            name = mOfficeList.get(position).getName();
        } else if (mType.equals("hospitals")) {
            name = mHospitalList.get(position).getName();
        }
        holder.mContentTV.setText("" + name);
    }

    @Override
    public int getItemCount() {
        if (mType.equals("offices")) {
            return mOfficeList.size();
        } else if (mType.equals("hospitals")) {
            return mHospitalList.size();
        }
        return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mContentTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mContentTV = (TextView) itemView.findViewById(R.id.universal_content);
        }
    }
}
