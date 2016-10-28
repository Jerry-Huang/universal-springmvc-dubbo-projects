package com.universal.web.user.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.universal.api.message.service.MessageService;
import com.universal.api.user.bean.UserBean;
import com.universal.api.user.service.UserService;
import com.universal.web.message.ApiResponse;
import com.universal.web.message.SimpleApiRequest;
import com.universal.web.message.SimpleApiResponse;
import com.universal.web.support.annotation.ApiRequestParam;
import com.universal.web.support.annotation.Validation;
import com.universal.web.utils.UserUtils;

@Controller
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private MessageService messageService;

    @RequestMapping(value = "/user/my.shtml")
    @ResponseBody
    @Validation({ "login-required,请先登录" })
    public ApiResponse findUserById(@ApiRequestParam final SimpleApiRequest apiRequest, final HttpServletRequest httpRequest) throws Exception {

        UserBean userBean = UserUtils.findCachedUser(apiRequest.getToken());

        int userId = userBean.getId();
        UserBean user = userService.findById(userId);
        SimpleApiResponse simpleApiResponse = new SimpleApiResponse();
        String userName = StringUtils.EMPTY;
        String face = StringUtils.EMPTY;
        String linkName = StringUtils.EMPTY;

        if (user != null) {
            userName = user.getUserName();
            face = user.getFace();
            linkName = user.getLinkName();
        }

        simpleApiResponse.addObject("user-id", userId);
        simpleApiResponse.addObject("user-name", userName);
        simpleApiResponse.addObject("face", face);
        simpleApiResponse.addObject("link-name", linkName);

        return simpleApiResponse;
    }

}
