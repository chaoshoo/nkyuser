package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 挂号医院科室
 * Created by xuchun on 2016/9/19.
 */
public class Dept implements Serializable {

    private String deptid;

    private String deptname;

    private String level;

    private String parentid;

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }
}