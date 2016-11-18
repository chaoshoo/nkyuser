package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 医生排班
 * Created by xuchun on 2016/9/22.
 */
public class Schedule implements Serializable {

    private Double orderfee;

    private String outpdate;

    private String scheduleid;

    private String timeinterval;

    private String validflag;

    public Double getOrderfee() {
        return orderfee;
    }

    public void setOrderfee(Double orderfee) {
        this.orderfee = orderfee;
    }

    public String getOutpdate() {
        return outpdate;
    }

    public void setOutpdate(String outpdate) {
        this.outpdate = outpdate;
    }

    public String getScheduleid() {
        return scheduleid;
    }

    public void setScheduleid(String scheduleid) {
        this.scheduleid = scheduleid;
    }

    public String getTimeinterval() {
        return timeinterval;
    }

    public void setTimeinterval(String timeinterval) {
        this.timeinterval = timeinterval;
    }

    public String getValidflag() {
        return validflag;
    }

    public void setValidflag(String validflag) {
        this.validflag = validflag;
    }
}
