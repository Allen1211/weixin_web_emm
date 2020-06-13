package com.allen.imsystem.common.exception;

import com.allen.imsystem.common.bean.ErrMsg;

import java.util.Map;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {
    private int code;
    private String message;
    private ErrMsg errMsg = new ErrMsg();

    public BusinessException(ExceptionType exceptionType) {
        this.code = exceptionType.getCode();
        this.message = exceptionType.getErrMsgText();
        this.errMsg.setText(exceptionType.getErrMsgText());
    }

    public BusinessException(ExceptionType exceptionType,String message) {
        this.code = exceptionType.getCode();
        this.message = message;
        this.errMsg.setText(message);
    }
    public BusinessException(ExceptionType exceptionType, Map<String,String> parameters ,String lastErrMsg) {
        this.code = exceptionType.getCode();
        this.message = exceptionType.getErrMsgText();
        this.errMsg.setText(lastErrMsg);
        this.errMsg.setParameters(parameters);
    }
    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ErrMsg getErrMsg(){
        return this.errMsg;
    }

}
