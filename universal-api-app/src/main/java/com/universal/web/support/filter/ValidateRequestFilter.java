package com.universal.web.support.filter;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.universal.exception.Code;
import com.universal.exception.ValidationException;

public class ValidateRequestFilter extends AbstractValidateRequestFilter {

    private static final long serialVersionUID = -6273671058500710729L;

    // @Autowired
    // private UserService userService;

    private Set<String> excludeUris = new HashSet<>();

    public void setExcludeUris(Set<String> excludeUris) {
        this.excludeUris = excludeUris;
    }

    @Override
    protected void validate(final ServletRequest request) throws ValidationException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (excludeUris.contains(httpRequest.getRequestURI())) {
            return;
        }

        final String token = StringUtils.defaultString(request.getParameter("token"));
        // final String deviceUuid =
        // StringUtils.defaultString(request.getParameter("device-uuid"));

        if (StringUtils.isBlank(token)) {
            throw new ValidationException(Code.SYS_ILLEGAL_TOKEN, "Token不能为空");
        }

        // final TokenBean tokenBean = userService.findLastToken(deviceUuid);
        //
        // if (tokenBean == null) {
        // throw new ValidationException(Code.SYS_ILLEGAL_TOKEN,
        // String.format("没有找到此设备 %s 的TOKEN", deviceUuid));
        // } else if (!tokenBean.getToken().equals(token)) {
        // throw new ValidationException(Code.SYS_ILLEGAL_TOKEN,
        // String.format("Token: %s, but last token = %s", token,
        // tokenBean.getToken()));
        // } else if (!"Y".equals(tokenBean.getStatus())) {
        // throw new ValidationException(Code.SYS_EXPIRED_TOKEN,
        // String.format("Token: %s 已过期", token));
        // }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
