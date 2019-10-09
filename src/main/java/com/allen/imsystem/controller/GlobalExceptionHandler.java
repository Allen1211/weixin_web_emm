package com.allen.imsystem.controller;


import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.dto.ErrMsg;
import com.allen.imsystem.model.dto.JSONResponse;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.ibatis.binding.BindingException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvocationTargetException.class)
    @ResponseBody
    public JSONResponse handleInvocationTargetException(InvocationTargetException ex){
        Throwable target = ex.getTargetException();
        try{
            BusinessException be = (BusinessException) target;
            return handleBusinessException(be);
        }catch (Exception e){
            return handleUnKnownException(ex);
        }
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public JSONResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
        throw new BusinessException(ExceptionType.REQUEST_METHOD_WRONG);
    }

    @ExceptionHandler(MySQLIntegrityConstraintViolationException.class)
    @ResponseBody
    public JSONResponse handleMySQLIntegrityConstraintViolationException(MySQLIntegrityConstraintViolationException ex){
        throw new BusinessException(ExceptionType.DATA_NOT_UNIQUE);
    }



    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public JSONResponse handleHttpMediaTypeNotSupportedException(HttpRequestMethodNotSupportedException ex){
        throw new BusinessException(ExceptionType.SERVER_ERROR, "不支持的content-type");
    }


    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public JSONResponse handleBusinessException(BusinessException be){
        JSONResponse jsonResponse = new JSONResponse();
        jsonResponse.setStatus(0);
        jsonResponse.setCode(be.getCode());
        if(be.getErrMsg() == null){
            jsonResponse.setErrMsg(be.getMessage());
        }else{
            jsonResponse.setErrMsg(be.getErrMsg());
        }
        return jsonResponse;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindingException.class})
    @ResponseBody
    public JSONResponse handleValidationException(Exception ex, HttpServletResponse response){
        BindingResult bindingResult = null;
        if (ex instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
        } else if(ex instanceof BindException) {
            bindingResult = ((BindException) ex).getBindingResult();
        } else {
            //other exception , ignore
        }

        if(bindingResult != null && bindingResult.hasErrors()) {
            Map<String,String> parameters = new HashMap<>();
            String lastMessage = "";
            for(FieldError error : bindingResult.getFieldErrors()){
                parameters.put(error.getField(),error.getDefaultMessage());
                lastMessage = error.getDefaultMessage();
            }
            return new JSONResponse(0,1002,new ErrMsg(lastMessage,parameters));
        }
        return null;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public JSONResponse handleOtherRuntimeException(Exception e){
        e.printStackTrace();
        if(e instanceof ClassCastException || e instanceof NumberFormatException){
            return handleBusinessException(new BusinessException(ExceptionType.PARAMETER_ILLEGAL));
        }else{
            return handleUnKnownException(e);
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JSONResponse handleUnKnownException(Exception e){
        e.printStackTrace();
        if(e instanceof MissingServletRequestParameterException){   //缺少参数
            return handleBusinessException(new BusinessException(ExceptionType.MISSING_PARAMETER_ERROR));
        }
        JSONResponse jsonResponse = new JSONResponse();
        jsonResponse.setStatus(0);
        jsonResponse.setCode(1000);
        jsonResponse.setErrMsg("未知错误");
        return jsonResponse;
    }
}
