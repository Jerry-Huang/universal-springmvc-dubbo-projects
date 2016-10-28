package com.universal.api.message.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.universal.entity.AbstractEntity;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsBean extends AbstractEntity {

    private static final long serialVersionUID = -1441136900895194575L;

    private String phone;
    private String content;

    public SmsBean() {

    }

    public SmsBean(String phone, String content) {
        this.phone = phone;
        this.content = content;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
