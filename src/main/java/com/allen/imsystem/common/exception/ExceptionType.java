package com.allen.imsystem.common.exception;

public enum ExceptionType {

    SERVER_ERROR(1000,"服务器错误"),
    MISSING_PARAMETER_ERROR(1001,"缺少参数"),
    PARAMETER_ILLEGAL(1002, "参数格式不合法"),
    USERNAME_PASSWORD_ERROR(1005, "用户名或密码错误"),
    NO_LOGIN_ERROR(1003, "请先登录"),
    TOKEN_EXPIRED_ERROR(1004,"token信息过期，请重新登录"),
    USER_NOT_FOUND(1006, "用户不存在"),
    URL_NOT_FOUND(1404, "URL不存在"),
    EMAIL_HAS_BEEN_REGISTED(1007,"邮箱已被注册"),
    EMAIL_CODE_WRONG(1008,"输入的邮箱验证码错误或已过期"),
    REQUEST_METHOD_WRONG(1009,"请求方法错误"),
    DATA_CONSTRAINT_FAIL(1010,"不满足数据约束。可能破坏外键约束，唯一性约束")
    ;

    private Integer code;

    private String errMsgText;

    ExceptionType(Integer code, String errMsgText) {
        this.code = code;
        this.errMsgText = errMsgText;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrMsgText() {
        return errMsgText;
    }

    public void setErrMsgText(String errMsgText) {
        this.errMsgText = errMsgText;
    }
}
