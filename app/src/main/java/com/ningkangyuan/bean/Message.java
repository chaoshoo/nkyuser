package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * Created by xuchun on 2016/8/25.
 */
public class Message implements Serializable {
    private String id;
    private String content;
    private String title;
    private String status;
    private String msg_type;
    private String create_time;
    private String vip_id;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getVip_id() {
        return vip_id;
    }

    public void setVip_id(String vip_id) {
        this.vip_id = vip_id;
    }
}
