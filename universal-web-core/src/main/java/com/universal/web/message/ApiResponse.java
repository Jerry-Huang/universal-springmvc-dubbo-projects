package com.universal.web.message;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse implements Serializable {

    private static final long serialVersionUID = 163079240370049305L;

    private int code;
    private String debug;
    private String token;
    private String version;
    private String message = "OK";

    protected ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse() {

    }

    public ApiResponse(final ApiRequest request) {

        this.setVersion(request.getVersion());
        this.setToken(request.getToken());
    }

    public ApiResponse(final String version, final String token) {

        this.setVersion(version);
        this.setToken(token);
    }

    public ApiResponse(final ApiRequest request, final String json) throws JsonParseException, JsonMappingException,
            IOException {

        this(json);

        this.setVersion(request.getVersion());
        this.setToken(request.getToken());
    }

    public ApiResponse(final String version, final String token, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        this(json);

        this.setVersion(version);
        this.setToken(token);
    }

    public ApiResponse(final String json) throws JsonParseException, JsonMappingException, IOException {

        if (StringUtils.isNotBlank(json)) {
            ApiResponse response = objectMapper.readValue(json, ApiResponse.class);
            BeanUtils.copyProperties(response, this);
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
