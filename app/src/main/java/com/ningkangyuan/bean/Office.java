package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 科室
 * Created by xuchun on 2016/8/23.
 */
public class Office implements Serializable {
    private String id;

    private String des;

    private String description;

    private String name;

    private String create_time;

    private String code;

    private String pic;

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id='" + id + '\'' +
                ", des='" + des + '\'' +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", create_time='" + create_time + '\'' +
                ", code='" + code + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}