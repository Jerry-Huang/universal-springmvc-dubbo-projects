package com.universal.web.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleApiResponse extends GenericApiResponse<Map<String, Object>> {

    private static final long serialVersionUID = 3204932303813549069L;

    public SimpleApiResponse() {

        this.setResponse(new HashMap<String, Object>());
    }

    public SimpleApiResponse(final ApiRequest request) {

        super(request);
        this.setResponse(new HashMap<String, Object>());
    }

    public SimpleApiResponse(final String version, final String token) {

        super(version, token);
        this.setResponse(new HashMap<String, Object>());
    }

    public SimpleApiResponse(final ApiRequest request, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(request);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<SimpleApiResponse>() {
        }), this);
    }

    public SimpleApiResponse(final String version, final String token, final String json) throws JsonParseException,
            JsonMappingException, IOException {

        super(version, token);
        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<SimpleApiResponse>() {
        }), this);
    }

    public SimpleApiResponse(final String json) throws JsonParseException, JsonMappingException, IOException {

        BeanUtils.copyProperties(objectMapper.readValue(json, new TypeReference<SimpleApiResponse>() {
        }), this);
    }

    public void addObject(final String name, final Object value) {

        this.getResponse().put(name, value);
    }
}
