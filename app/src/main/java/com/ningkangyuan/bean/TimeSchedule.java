package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 分时排班
 * Created by xuchun on 2016/9/22.
 */
public class TimeSchedule implements Serializable {

    private String bstp;

    private String estp;

    private String partscheduleid;

    private String regflag;

    private String scheduleid;

    public String getBstp() {
        return bstp;
    }

    public void setBstp(String bstp) {
        this.bstp = bstp;
    }

    public String getEstp() {
        return estp;
    }

    public void setEstp(String estp) {
        this.estp = estp;
    }

    public String getPartscheduleid() {
        return partscheduleid;
    }

    public void setPartscheduleid(String partscheduleid) {
        this.partscheduleid = partscheduleid;
    }

    public String getRegflag() {
        return regflag;
    }

    public void setRegflag(String regflag) {
        this.regflag = regflag;
    }

    public String getScheduleid() {
        return scheduleid;
    }

    public void setScheduleid(String scheduleid) {
        this.scheduleid = scheduleid;
    }
}
