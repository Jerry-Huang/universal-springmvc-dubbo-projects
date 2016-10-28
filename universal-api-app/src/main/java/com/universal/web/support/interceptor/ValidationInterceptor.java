package com.universal.web.support.interceptor;

import com.universal.api.user.bean.UserBean;
import com.universal.web.support.interceptor.AbstractValidationInterceptor;
import com.universal.web.utils.UserUtils;

public class ValidationInterceptor extends AbstractValidationInterceptor {

    protected boolean hasLogin(final String token) {

        UserBean userBean = UserUtils.findCachedUser(token);

        return userBean != null;
    }
}
