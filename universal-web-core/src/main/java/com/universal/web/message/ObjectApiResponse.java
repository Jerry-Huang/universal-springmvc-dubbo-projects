package com.universal.web.message;

import java.io.IOException;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectApiResponse extends GenericApiResponse<Object> {

    private static final long serialVersionUID = 3204932303813549069L;

    public ObjectApiResponse() {

    }

    public ObjectApiResponse(final Object response) {

        this.setResponse(response);
    }

    public ObjectApiResponse(final ApiRequest request, final Object response) {

        super(request);
        this.setResponse(response);
    }

    public ObjectApiResponse(final String version, final String token, final Object response) {

        super(version, token);
        this.setResponse(response);
    }

    public ObjectApiResponse(final ApiRequest request) {

        super(request);
    }

    public ObjectApiResponse(final String version, final String token) {

        super(version, token);
    }

    public ObjectApiResponse(final ApiRequest request, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(request);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<ObjectApiResponse>() {
        }), this);
    }

    public ObjectApiResponse(final String version, final String token, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(version, token);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<ObjectApiResponse>() {
        }), this);
    }

    public ObjectApiResponse(final String json) throws JsonParseException, JsonMappingException, IOException {

        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<ObjectApiResponse>() {
        }), this);
    }
}
