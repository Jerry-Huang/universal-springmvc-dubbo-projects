package com.universal.web.support.filter;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universal.Environment;
import com.universal.exception.Code;
import com.universal.exception.ValidationException;
import com.universal.utils.HttpUtils;
import com.universal.utils.VersionUtils;
import com.universal.web.message.ApiResponse;

public abstract class AbstractValidateRequestFilter extends HttpServlet implements Filter {

    private static final long serialVersionUID = 4387130682179084293L;

    private final static Logger logger = LoggerFactory.getLogger(AbstractValidateRequestFilter.class);

    private Map<String, Pair<String, String>> bundles = null;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (Environment.current() != Environment.DEV) {
            try {
                this.validateParameter(httpRequest);
                this.validateVersion(httpRequest);
                this.validateBundle(httpRequest);
                this.validateSign(httpRequest);

                this.validate(httpRequest);
            } catch (ValidationException e) {

                logger.error("Illegal request", e);

                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setCode(e.getCode().getCode());

                if (Environment.current() != Environment.PRD) {
                    apiResponse.setDebug(e.getDebug());
                }

                apiResponse.setMessage(e.getMessage());
                apiResponse.setToken(httpRequest.getParameter("token"));
                apiResponse.setVersion(httpRequest.getParameter("version"));

                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.reset();
                httpResponse.setHeader("Content-Type", "application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_OK);

                ObjectMapper objectMapper = new ObjectMapper();
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

                return;
            }
        }

        chain.doFilter(httpRequest, response);
    }

    protected abstract void validate(final ServletRequest request) throws ValidationException;

    private void validateSign(final ServletRequest request) throws ValidationException {

        final String sign = StringUtils.defaultString(request.getParameter("sign"));

        final Map<String, String> parameterMap = new TreeMap<>(new Comparator<String>() {

            public int compare(String p1, String p2) {
                return p1.compareTo(p2);
            }
        });

        Map<String, String[]> requestParameters = request.getParameterMap();

        for (Map.Entry<String, String[]> entity : requestParameters.entrySet()) {

            if (!entity.getKey().equals("sign")) {
                parameterMap.put(entity.getKey(), StringUtils.join(entity.getValue(), ","));
            }
        }

        final String queryString = HttpUtils.generateQueryString(parameterMap);
        logger.debug("Unsigned: {}", queryString);

        final String signed = DigestUtils.md5Hex(queryString);
        logger.debug("Signed: {}", signed);

        if (!signed.equalsIgnoreCase(sign)) {
            throw new ValidationException(Code.SYS_ILLEGAL_SIGN, String.format("签名校验失败, Unsigned: %s, sign = %s, but expected sign = %s", queryString, sign, signed));
        }
    }

    private void validateBundle(final ServletRequest request) throws ValidationException {

        final String bundle = StringUtils.defaultString(request.getParameter("bundle"));
        final String version = StringUtils.defaultString(request.getParameter("version"));

        if (bundles.containsKey(bundle)) {

            final Pair<String, String> versionDeviceOS = bundles.get(bundle);

            if (!version.matches(versionDeviceOS.getKey())) {
                throw new ValidationException(Code.SYS_ILLEGAL_BUNDLE, String.format("BUNDLE %s 与VERSION %s 不匹配", bundle, version));
            }

            final String deviceOS = StringUtils.defaultString(request.getParameter("device-os"));
            if (StringUtils.isNotBlank(deviceOS)) {
                if (!deviceOS.matches(versionDeviceOS.getValue())) {
                    throw new ValidationException(Code.SYS_ILLEGAL_BUNDLE, String.format("BUNDLE %s 与OS %s 不匹配", bundle, deviceOS));
                }
            }
        } else {
            throw new ValidationException(Code.SYS_ILLEGAL_BUNDLE, String.format("非法的BUNDLE %s", bundle));
        }
    }

    private void validateVersion(final ServletRequest request) throws ValidationException {

        final String version = request.getParameter("version");

        if (VersionUtils.isBefor2_1(version)) {
            throw new ValidationException(Code.SYS_INCOMPATIBLE_VERSION, String.format("当前版本 %s 已过期，请升级到最新版本。", version));
        }
    }

    private void validateParameter(final ServletRequest request) throws ValidationException {

        final String sign = StringUtils.defaultString(request.getParameter("sign"));
        final String bundle = StringUtils.defaultString(request.getParameter("bundle"));
        final String version = StringUtils.defaultString(request.getParameter("version"));
        final String sequence = StringUtils.defaultString(request.getParameter("sequence"));
        final String timestamp = StringUtils.defaultString(request.getParameter("timestamp"));
        final String deviceUuid = StringUtils.defaultString(request.getParameter("device-uuid"));

        if (StringUtils.isBlank(sign)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "签名不能为空");
        } else if (StringUtils.isBlank(bundle)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Bundle不能为空");
        } else if (StringUtils.isBlank(deviceUuid)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "UUID不能为空");
        } else if (StringUtils.isBlank(version)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Version不能为空");
        } else if (StringUtils.isBlank(sequence)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Sequence不能为空");
        } else if (StringUtils.isBlank(timestamp)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Timestamp不能为空");
        }
    }

    public void setBundles(Map<String, Pair<String, String>> bundles) {
        this.bundles = bundles;
    }

}
