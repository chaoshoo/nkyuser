package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 挂号医生
 * Created by xuchun on 2016/9/19.
 */
public class Doc implements Serializable {
    private String GH_COUNT;
    private String deptid;
    private String deptname;
    private String docdes;
    private String docid;
    private String docname;
    private String doctitle;
    private String smallpicurl;

    public String getGH_COUNT() {
        return GH_COUNT;
    }

    public void setGH_COUNT(String GH_COUNT) {
        this.GH_COUNT = GH_COUNT;
    }

    public String getDeptid() {
        return deptid;
    }

    public void setDeptid(String deptid) {
        this.deptid = deptid;
    }

    public String getDeptname() {
        return deptname;
    }

    public void setDeptname(String deptname) {
        this.deptname = deptname;
    }

    public String getDocdes() {
        return docdes;
    }

    public void setDocdes(String docdes) {
        this.docdes = docdes;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public String getDocname() {
        return docname;
    }

    public void setDocname(String docname) {
        this.docname = docname;
    }

    public String getDoctitle() {
        return doctitle;
    }

    public void setDoctitle(String doctitle) {
        this.doctitle = doctitle;
    }

    public String getSmallpicurl() {
        return smallpicurl;
    }

    public void setSmallpicurl(String smallpicurl) {
        this.smallpicurl = smallpicurl;
    }
}
