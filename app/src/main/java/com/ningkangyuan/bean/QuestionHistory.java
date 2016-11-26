package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * 咨询(留言)
 * Created by xuchun on 2016/8/25.
 */
public class QuestionHistory implements Serializable {

    private String id;
    private String content;
    private String attachement;
    private String title;
    private String vip_code;
    private String create_time;
    private String doctor_code;
    private String last_reply_time;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachement() {
        return attachement;
    }

    public void setAttachement(String attachement) {
        this.attachement = attachement;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVip_code() {
        return vip_code;
    }

    public void setVip_code(String vip_code) {
        this.vip_code = vip_code;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getDoctor_code() {
        return doctor_code;
    }

    public void setDoctor_code(String doctor_code) {
        this.doctor_code = doctor_code;
    }

    public String getLast_reply_time() {
        return last_reply_time;
    }

    public void setLast_reply_time(String last_reply_time) {
        this.last_reply_time = last_reply_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}