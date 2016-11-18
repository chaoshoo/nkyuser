package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 检测指标
 * Created by xuchun on 2016/8/24.
 */
public class DetectionKPI implements Serializable {
    private String id;

    private String kpi_code;

    private String inspect_time;

    private String card_code;

    private String inspect_value;

    //0：normal -1：low 1：high
    private String inspect_is_normal;

    private String create_time;

    private String inspect_code;

    private String inspect_name;

    private String inspect_desc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKpi_code() {
        return kpi_code;
    }

    public void setKpi_code(String kpi_code) {
        this.kpi_code = kpi_code;
    }

    public String getInspect_time() {
        return inspect_time;
    }

    public void setInspect_time(String inspect_time) {
        this.inspect_time = inspect_time;
    }

    public String getCard_code() {
        return card_code;
    }

    public void setCard_code(String card_code) {
        this.card_code = card_code;
    }

    public String getInspect_value() {
        return inspect_value;
    }

    public void setInspect_value(String inspect_value) {
        this.inspect_value = inspect_value;
    }

    public String getInspect_is_normal() {
        return inspect_is_normal;
    }

    public void setInspect_is_normal(String inspect_is_normal) {
        this.inspect_is_normal = inspect_is_normal;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getInspect_code() {
        return inspect_code;
    }

    public void setInspect_code(String inspect_code) {
        this.inspect_code = inspect_code;
    }

    public String getInspect_name() {
        return inspect_name;
    }

    public void setInspect_name(String inspect_name) {
        this.inspect_name = inspect_name;
    }

    public String getInspect_desc() {
        return inspect_desc;
    }

    public void setInspect_desc(String inspect_desc) {
        this.inspect_desc = inspect_desc;
    }
}
