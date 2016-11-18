package com.ningkangyuan.bean;

import java.io.Serializable;

/**
 * Created by xuchun on 2016/9/8.
 */
public class QuestionLog implements Serializable {

    private String id;
    private String answer_code;
    private String create_time;
    private String vip_questions_id;
    private String answer_content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswer_code() {
        return answer_code;
    }

    public void setAnswer_code(String answer_code) {
        this.answer_code = answer_code;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getVip_questions_id() {
        return vip_questions_id;
    }

    public void setVip_questions_id(String vip_questions_id) {
        this.vip_questions_id = vip_questions_id;
    }

    public String getAnswer_content() {
        return answer_content;
    }

    public void setAnswer_content(String answer_content) {
        this.answer_content = answer_content;
    }
}
