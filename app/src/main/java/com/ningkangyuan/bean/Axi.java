package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 检测设备种类
 * Created by xuchun on 2016/9/2.
 */
public class Axi implements Serializable {

    public Axi(String name,String code,int icon) {
        this.setName(name);
        this.setCode(code);
        this.setIcon(icon);
    }

    private String name;
    private String code;
    private int icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
