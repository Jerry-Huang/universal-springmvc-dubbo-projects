package com.universal.api.user.service;

import com.universal.api.user.bean.UserBean;

public interface UserService {

    UserBean findById(final int userId);

}
