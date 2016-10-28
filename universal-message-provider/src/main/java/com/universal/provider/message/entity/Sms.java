package com.universal.provider.message.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Sms implements Serializable {

    private static final long serialVersionUID = 8139612561110635575L;

    private long id;
    private String ip;
    private String to;
    private String from;
    private String status;
    private String content;
    private String channel;
    private Date createTime;

    private String templateCode;
    private Map<String, String> parameters = null;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Sms() {
    }

    public Sms(final String to, String content) {
        this.to = to;
        this.content = content;
    }

    public Sms(String to, String from, String content, String ip, Date createTime) {
        super();
        this.to = to;
        this.from = from;
        this.content = content;
        this.ip = ip;
        this.createTime = createTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {

        String jsonParameters = StringUtils.EMPTY;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            jsonParameters = objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
        }

        return String.format("from=%s, to=%s, content=%s, id=%s, ip=%s, createTime=%s, status=%s, templateCode=%s, parameters=%s", from, to, content, id, ip, createTime, status,
                templateCode, jsonParameters);
    }
}
