package com.universal.web.support.resolver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import com.universal.Environment;
import com.universal.exception.Code;
import com.universal.exception.UniversalException;
import com.universal.web.message.ApiResponse;

@ControllerAdvice
public class DefaultExceptionResolver {

    private final static Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    @ExceptionHandler({ Exception.class })
    @ResponseBody
    public ApiResponse resolveException(NativeWebRequest httpRequest, Exception exception) {

        logger.error(Code.SYS_SERVICE_ERROR.toString(), exception);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(Code.SYS_SERVICE_ERROR.getCode());
        apiResponse.setMessage(Code.SYS_SERVICE_ERROR.getMessage());

        if (Environment.current() != Environment.PRD) {
            apiResponse.setDebug(ExceptionUtils.getStackTrace(exception));
        }

        apiResponse.setToken(httpRequest.getParameter("token"));
        apiResponse.setVersion(httpRequest.getParameter("version"));

        return apiResponse;
    }

    @ExceptionHandler({ UniversalException.class })
    @ResponseBody
    public ApiResponse resolveUniversalException(NativeWebRequest httpRequest, UniversalException exception) {

        logger.error(exception.toString(), exception);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(exception.getCode().getCode());
        apiResponse.setMessage(exception.getMessage());

        if (Environment.current() != Environment.PRD) {
            if (StringUtils.isNotBlank(exception.getDebug())) {
                apiResponse.setDebug(exception.getDebug());
            } else {
                apiResponse.setDebug(ExceptionUtils.getStackTrace(exception));
            }
        }

        apiResponse.setToken(httpRequest.getParameter("token"));
        apiResponse.setVersion(httpRequest.getParameter("version"));

        return apiResponse;
    }

}
