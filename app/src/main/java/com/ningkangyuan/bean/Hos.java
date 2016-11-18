package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 挂号医院
 * Created by xuchun on 2016/9/19.
 */
public class Hos implements Serializable {

    private String hosname;

    private String hosid;

    private String ispay;

    private String mdt;

    private String parenthosid;

    public String getHosname() {
        return hosname;
    }

    public void setHosname(String hosname) {
        this.hosname = hosname;
    }

    public String getHosid() {
        return hosid;
    }

    public void setHosid(String hosid) {
        this.hosid = hosid;
    }

    public String getIspay() {
        return ispay;
    }

    public void setIspay(String ispay) {
        this.ispay = ispay;
    }

    public String getMdt() {
        return mdt;
    }

    public void setMdt(String mdt) {
        this.mdt = mdt;
    }

    public String getParenthosid() {
        return parenthosid;
    }

    public void setParenthosid(String parenthosid) {
        this.parenthosid = parenthosid;
    }
}
