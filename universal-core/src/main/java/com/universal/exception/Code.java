package com.universal.exception;

public enum Code {

                  SYS_UNKNOWN(-10000, "未知错误"),
                  SYS_SERVICE_ERROR(-10001, "服务错误"),
                  SYS_NOT_FOUND(-10002, "没有找到数据"),
                  SYS_ILLEGAL_SIGN(-10003, "无效的签名"),
                  SYS_ILLEGAL_ARGUMENT(-10004, "无效的参数"),
                  SYS_ILLEGAL_TOKEN(-10005, "无效的TOKEN"),
                  SYS_EXPIRED_TOKEN(-10006, "TOKEN已过期"),
                  SYS_ILLEGAL_BUNDLE(-10007, "无效的BUNDLE"),
                  SYS_INCOMPATIBLE_VERSION(-10008, "版本不兼容"),

                  USR_ACCOUT_DISABLED(-11001, "账号已停用"),
                  USR_INVALID_USERNAME(-11002, "无效的用户名或密码"),
                  USR_LOGIN_REQUIRED(-11003, "要求先登录"),
                  USR_PERMISSION_DENIED(-11004, "没有权限");

    private int code;
    private String message;

    private Code(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {

        return String.format("CODE: %s, %s", this.code, this.message);
    }
}
