package com.universal.web.message;

import java.math.BigDecimal;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.universal.utils.DESUtils;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleApiRequest extends GenericApiRequest<JSONObject> {

    private static final long serialVersionUID = 960913632168413748L;

    public SimpleApiRequest() {

    }

    public SimpleApiRequest(final String json) throws Exception {

        if (StringUtils.isNotBlank(json)) {
            SimpleApiRequest request = objectMapper.readValue(json, new TypeReference<SimpleApiRequest>() {
            });
            BeanUtils.copyProperties(request, this);

            if (StringUtils.isNotBlank(request.getRawRequest())) {
                setRequest(JSON.parseObject(request.getRawRequest()));
            }
        }
    }

    public String getString(final String name) {

        if (this.getRequest() != null) {
            return StringUtils.defaultString(this.getRequest().getString(name));
        }

        return StringUtils.EMPTY;
    }

    public String getPlaintext(final String name) throws Exception {
        String cipher = getString(name);
        if (StringUtils.isBlank(this.getToken()) || StringUtils.isBlank(cipher)) {
            return StringUtils.EMPTY;
        }
        String key = DigestUtils.md5Hex(this.getToken()).substring(0, 24);
        return DESUtils.decrypt(cipher, key);
    }

    public JSONObject get(final String name) {

        if (this.getRequest() != null) {
            return this.getRequest().getJSONObject(name);
        }

        return null;
    }
    
    public JSONArray getArray(final String name) {

        if (this.getRequest() != null) {
            return this.getRequest().getJSONArray(name);
        }

        return null;
    }

    public int getInt(final String name) {

        return NumberUtils.toInt(getString(name));
    }

    public double getDouble(final String name) {

        return NumberUtils.toDouble(getString(name));
    }

    public BigDecimal getBigDecimal(final String name) {

        final String stringValue = getString(name);

        if (StringUtils.isNotBlank(stringValue) && !stringValue.equals("null")) {
            return new BigDecimal(stringValue);
        } else {
            return BigDecimal.ZERO;
        }
    }

}
