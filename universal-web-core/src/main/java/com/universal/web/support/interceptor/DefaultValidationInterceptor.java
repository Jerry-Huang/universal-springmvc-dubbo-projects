package com.universal.web.support.interceptor;

public class DefaultValidationInterceptor extends AbstractValidationInterceptor {

    protected boolean hasLogin(final String token) {
        return true;
    }
}
