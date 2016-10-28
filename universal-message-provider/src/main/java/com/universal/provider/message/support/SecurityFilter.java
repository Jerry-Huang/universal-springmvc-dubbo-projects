package com.universal.provider.message.support;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universal.exception.Code;
import com.universal.exception.ValidationException;
import com.universal.utils.HttpUtils;

public class SecurityFilter implements ContainerRequestFilter {

    private final Properties properties = new Properties();

    private final Set<String> clientIpBlackSet = new HashSet<String>();
    private final Set<String> requestIpWhiteSet = new HashSet<String>();
    private final Set<String> logExcludeUriSet = new HashSet<String>();

    @Context
    private HttpServletRequest httpServletRequest;

    private final static Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    public SecurityFilter() throws IOException {

        final ClassLoader classLoader = this.getClass().getClassLoader();
        properties.load(classLoader.getResourceAsStream("sms.properties"));

        final String clientIpBlackList = properties.getProperty("sms.client.ip.black.list");
        final String requestIpWhiteList = properties.getProperty("sms.request.ip.white.list");
        final String logExcludeUriList = properties.getProperty("sms.log.exclude.uri.list");

        if (StringUtils.isNotBlank(clientIpBlackList)) {
            String[] slices = clientIpBlackList.split(",");
            for (String slice : slices) {
                if (StringUtils.isNotBlank(slice)) {
                    clientIpBlackSet.add(slice.trim());
                }
            }
        }

        if (StringUtils.isNotBlank(requestIpWhiteList)) {
            String[] slices = requestIpWhiteList.split(",");
            for (String slice : slices) {
                if (StringUtils.isNotBlank(slice)) {
                    requestIpWhiteSet.add(slice.trim());
                }
            }
        }

        if (StringUtils.isNotBlank(logExcludeUriList)) {
            String[] slices = logExcludeUriList.split(",");
            for (String slice : slices) {
                if (StringUtils.isNotBlank(slice)) {
                    logExcludeUriSet.add(slice.trim());
                }
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String requestUri = httpServletRequest.getRequestURI();

        if (logExcludeUriSet.contains(requestUri)) {
            return;
        }

        logger.info("IP: {}", HttpUtils.realIp(httpServletRequest));
        logger.info("URL: {}", httpServletRequest.getRequestURI());

        Map<String, String[]> parameters = httpServletRequest.getParameterMap();
        for (Map.Entry<String, String[]> entity : parameters.entrySet()) {

            logger.info(String.format("%s = %s", entity.getKey(), StringUtils.join(entity.getValue(), ",")));
        }

        try {
            this.validate(httpServletRequest);
        } catch (ValidationException e) {
            logger.error("Illegal request", e);
            requestContext.abortWith(Response.status(Response.Status.OK).entity(e.getCode() + ". " + e.getDebug() + ".").build());
        }
    }

    private void validate(final HttpServletRequest httpServletRequest) throws ValidationException {

        final String clientIp = httpServletRequest.getParameter("ip");
        final String requestIp = HttpUtils.realIp(httpServletRequest);

        if (StringUtils.isBlank(clientIp)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "The ip parametere is not found");
        }

        if (clientIpBlackSet.contains(clientIp)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Blocked client ip " + clientIp);
        }

        if (!requestIpWhiteSet.contains("N/A") && !requestIpWhiteSet.contains(requestIp)) {
            throw new ValidationException(Code.SYS_ILLEGAL_ARGUMENT, "Not allowed request ip " + requestIp);
        }
    }

}
