package com.universal.web.support.resolver;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universal.web.support.annotation.ApiRequestParam;

public class ApiRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(ApiRequestParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Class<?> clazz = parameter.getParameterType();

        Constructor<?> constructor = clazz.getConstructor(String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> jsonDeviceMap = new HashMap<>();
        Map<String, Object> jsonRequestMap = new HashMap<>();

        Map<String, String[]> parameters = webRequest.getParameterMap();

        for (Map.Entry<String, String[]> entity : parameters.entrySet()) {

            if (entity.getKey().startsWith("device-")) {
                jsonDeviceMap.put(entity.getKey(), StringUtils.join(entity.getValue(), ","));
            } else {
                jsonRequestMap.put(entity.getKey(), StringUtils.join(entity.getValue(), ","));
            }
        }

        jsonRequestMap.put("device", jsonDeviceMap);

        return constructor.newInstance(objectMapper.writeValueAsString(jsonRequestMap));

    }
}
