package com.ningkangyuan.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ningkangyuan.R;
import com.ningkangyuan.bean.Hos;
import com.ningkangyuan.bean.Order;
import com.ningkangyuan.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/21.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {

    private static final String TAG = "OrderAdapter";

    private List<Order> mOrderList;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public OrderAdapter(List<Order> orderList) {
        this.mOrderList = orderList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from((parent.getContext())).inflate(R.layout.order_item,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Order order = mOrderList.get(position);
        holder.mOrderNumTV.setText("预诊时间：" + order.getOutpdate() + "\n订单编号：" + order.getOrderid());
        String content = "订单金额：" + order.getOrderfee() + "元";
        String status = "";
        String orderTime = order.getCreate_time();
        String payTime = order.getPayrtime();
        String cancelTime = order.getCanceltime();
        try {
//            if (!TextUtils.isEmpty(order.getOrdertime())) {
//                orderTime = mSimpleDateFormat.format(new Date(Long.valueOf(order.getOrdertime())));
//            }
//            if (!TextUtils.isEmpty(order.getPayrtime())) {
//                payTime = mSimpleDateFormat.format(new Date(Long.valueOf(order.getPayrtime())));
//            }
//            if (!TextUtils.isEmpty(order.getCanceltime())) {
//                cancelTime = mSimpleDateFormat.format(new Date(Long.valueOf(order.getCanceltime())));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ("1".equals(order.getStatus())) {
            holder.mOrderStatusTV.setText("待支付");
        } else if ("2".equals(order.getStatus())) {
            content += "\n" + "确认时间：" + orderTime;
            status = "已确认";
        } else if ("3".equals(order.getStatus())) {
            content += "\n" + "确认时间：" + orderTime + "\n" + "支付时间：" + payTime;
            status = "已支付";
        } else if ("4".equals(order.getStatus())) {
            content += "\n" + "确认时间：" + orderTime;
            status = "支付失败";
        } else if ("5".equals(order.getStatus())) {
            content += "\n" + "取消时间：" + cancelTime + "\n" + "取消原因：" + order.getCancelreason();
            status = "已取消";
        } else if ("6".equals(order.getStatus())) {
            status = "挂号失败";
        }
        holder.mOrderContentTV.setText(content);
        holder.mOrderStatusTV.setText(status);
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mOrderNumTV;
        private TextView mOrderContentTV;
        private TextView mOrderStatusTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            mOrderNumTV = (TextView) itemView.findViewById(R.id.order_item_num);
            mOrderContentTV = (TextView) itemView.findViewById(R.id.order_item_content);
            mOrderStatusTV = (TextView) itemView.findViewById(R.id.order_item_status);
        }
    }
}
