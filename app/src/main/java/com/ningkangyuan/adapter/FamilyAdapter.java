package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Doc;
import com.ningkangyuan.bean.Vip;

import java.util.List;

/**
 * 成员管理
 * Created by xuchun on 2016/9/21.
 */
public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.MyViewHolder> {

    private List<Vip> mVipList;

    public FamilyAdapter(List<Vip> vipList) {
        this.mVipList = vipList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.family_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Vip vip = mVipList.get(position);
        if (position == 0) {
            holder.portrait.setImageResource(R.drawable.icon_family_bind);
        } else if (position == 1) {
            holder.portrait.setImageResource(R.drawable.main_erweima);
        } else {
            holder.portrait.setImageResource(R.drawable.icon_family_child);
        }
        holder.name.setText(vip.getReal_name());
    }

    @Override
    public int getItemCount() {
        return mVipList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView portrait;
        public TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            portrait = (ImageView) itemView.findViewById(R.id.family_item_portrait);
            name = (TextView) itemView.findViewById(R.id.family_item_name);
        }
    }
}