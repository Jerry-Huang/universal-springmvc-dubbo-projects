package com.universal.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {

    private static final long serialVersionUID = 783461266372478877L;

    public ObjectMapper() {
        super();
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }
}
