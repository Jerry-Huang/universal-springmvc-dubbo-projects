package com.universal.web.support.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.universal.utils.HttpUtils;

@Component
public class LogRequestFilter extends HttpServlet implements Filter {

    private static final long serialVersionUID = -354361364448750443L;

    private final static Logger logger = LoggerFactory.getLogger(LogRequestFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (logger.isInfoEnabled()) {

            final StringBuilder log = new StringBuilder();
            final HttpServletRequest httpRequest = (HttpServletRequest) request;

            log.append(httpRequest.getRequestURI()).append(", ");
            log.append("IP: " + HttpUtils.realIp(httpRequest)).append(", [");

            Map<String, String[]> parameters = request.getParameterMap();

            for (Map.Entry<String, String[]> entity : parameters.entrySet()) {

                log.append(String.format("%s = %s, ", entity.getKey(), StringUtils.join(entity.getValue(), ',')));
            }

            log.delete(log.length() - 2, log.length()).append("]");

            logger.info(log.toString());
        }

        chain.doFilter(request, response);
    }

}
