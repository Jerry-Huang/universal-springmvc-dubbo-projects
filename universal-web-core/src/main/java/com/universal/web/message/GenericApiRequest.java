package com.universal.web.message;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericApiRequest<T> extends ApiRequest {

    private static final long serialVersionUID = 960913632168413748L;

    private T request;

    public GenericApiRequest() {

    }

    public GenericApiRequest(final String json) throws Exception {

        if (StringUtils.isNotBlank(json)) {
            GenericApiRequest<T> response = objectMapper.readValue(json, new TypeReference<GenericApiRequest<T>>() {
            });
            BeanUtils.copyProperties(response, this);

            setRequest(response.getRawRequest());
        }
    }

    public GenericApiRequest(final String json, final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {

        if (StringUtils.isNotBlank(json)) {
            ObjectMapper objectMapper = new ObjectMapper();

            JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(GenericApiRequest.class, GenericApiRequest.class, clazz);
            GenericApiRequest<T> response = objectMapper.readValue(json, javaType);
            BeanUtils.copyProperties(response, this);
        }
    }

    @JsonIgnore
    public T getRequest() {

        return request;
    }

    public void setRequest(T request) {

        this.request = request;
    }

    public void setRequest(final String json) throws Exception {

        if (StringUtils.isNotBlank(json)) {
            this.request = objectMapper.readValue(super.getRawRequest(), new TypeReference<T>() {
            });
        }
    }

}
