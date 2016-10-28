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
public class GenericApiResponse<T> extends ApiResponse {

    private static final long serialVersionUID = 8703899092409779832L;

    private T response;

    public GenericApiResponse() {

    }

    public GenericApiResponse(final ApiRequest request) {

        super(request);
    }

    public GenericApiResponse(final String version, final String token) {

        super(version, token);
    }

    public GenericApiResponse(final ApiRequest request, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(request);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<GenericApiResponse<T>>() {
        }), this);
    }

    public GenericApiResponse(final String version, final String token, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(version, token);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<GenericApiResponse<T>>() {
        }), this);
    }

    public GenericApiResponse(final String json) throws JsonParseException, JsonMappingException, IOException {

        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<GenericApiResponse<T>>() {
        }), this);
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

}
