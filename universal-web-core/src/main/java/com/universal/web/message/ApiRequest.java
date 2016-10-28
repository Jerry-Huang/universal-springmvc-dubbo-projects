package com.universal.web.message;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRequest implements Serializable {

    private static final long serialVersionUID = -3797578697277913489L;

    private String sign;
    private String bundle;
    private String version;
    private Device device;
    private String token;
    private long sequence;
    private long timestamp;
    private String rawRequest;
    
    protected ObjectMapper objectMapper = new ObjectMapper();

    public ApiRequest() {

    }

    public ApiRequest(final String json) throws JsonParseException, JsonMappingException, IOException {

        if (StringUtils.isNotBlank(json)) {
            ApiRequest request = objectMapper.readValue(json, ApiRequest.class);
            BeanUtils.copyProperties(request, this);
        }
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("request")
    public String getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    @Override
    public String toString() {

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {

            return StringUtils.EMPTY;
        }

    }
}
