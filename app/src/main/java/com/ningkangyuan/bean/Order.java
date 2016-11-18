package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * Created by xuchun on 2016/9/28.
 */
public class Order implements Serializable {

    private String id;

    private String orderid;

    private String status;

    private String orderfee;

    private String payrtime;

    private String ordertime;

    private String orderconfirmsms;

    private String canceltime;

    private String cancelreason;

    private String hosname;

    private String deptname;

    private String docname;

    private String create_time;

    private String outpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderfee() {
        return orderfee;
    }

    public void setOrderfee(String orderfee) {
        this.orderfee = orderfee;
    }

    public String getPayrtime() {
        return payrtime;
    }

    public void setPayrtime(String payrtime) {
        this.payrtime = payrtime;
    }

    public String getOrdertime() {
        return ordertime;
    }

    public void setOrdertime(String ordertime) {
        this.ordertime = ordertime;
    }

    public String getOrderconfirmsms() {
        return orderconfirmsms;
    }

    public void setOrderconfirmsms(String orderconfirmsms) {
        this.orderconfirmsms = orderconfirmsms;
    }

    public String getCanceltime() {
        return canceltime;
    }

    public void setCanceltime(String canceltime) {
        this.canceltime = canceltime;
    }

    public String getCancelreason() {
        return cancelreason;
    }

    public void setCancelreason(String cancelreason) {
        this.cancelreason = cancelreason;
    }

    public String getHosname() {
        return hosname;
    }

    public void setHosname(String hosname) {
        this.hosname = hosname;
    }

    public String getDeptname() {
        return deptname;
    }

    public void setDeptname(String deptname) {
        this.deptname = deptname;
    }

    public String getDocname() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname = docname;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getOutpdate() {
        return outpdate;
    }

    public void setOutpdate(String outpdate) {
        this.outpdate = outpdate;
    }
}
