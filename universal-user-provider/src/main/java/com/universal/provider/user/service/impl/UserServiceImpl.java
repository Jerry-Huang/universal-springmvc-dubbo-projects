package com.universal.provider.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.universal.api.user.bean.UserBean;
import com.universal.api.user.service.UserService;
import com.universal.provider.user.mapper.UserMapper;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserBean findById(int userId) {

        final UserBean userBean = new UserBean();
        userBean.setId(userId);
        
        return userMapper.find(userBean);
    }

}
