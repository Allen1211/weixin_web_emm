package com.allen.imsystem.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.model.dto.ErrMsg;
import com.allen.imsystem.model.dto.JSONResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValidationExceptionResolver extends AbstractHandlerExceptionResolver {

    public ValidationExceptionResolver() {
        setOrder(0);
    }

    protected ModelAndView handleMethodArgumentNotValidException(BindingResult bindingResult, HttpServletRequest request,
                                                                 HttpServletResponse response, Object handler) {
        System.out.println("IN");
        if(bindingResult != null && bindingResult.hasErrors()){
            Map<String,String> parameters = new HashMap<>();
            String lastMessage = "";
            for(FieldError error : bindingResult.getFieldErrors()){
                parameters.put(error.getField(),error.getDefaultMessage());
                lastMessage = error.getDefaultMessage();
            }
            JSONResponse jsonResponse = new JSONResponse(0,1002,new ErrMsg(lastMessage,parameters));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                response.getWriter().write(JSONObject.toJSONString(jsonResponse));
            }catch(IOException e){
                e.printStackTrace();
                throw new BusinessException(ExceptionType.SERVER_ERROR);
            }
        }
        return null;
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BindingResult bindingResult = null;
        if (ex instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
        } else if(ex instanceof BindException) {
            bindingResult = ((BindException) ex).getBindingResult();
        } else {
            //other exception , ignore
        }

        if(bindingResult != null) {
            return handleMethodArgumentNotValidException(bindingResult, request, response, handler);
        }
        return null;
    }
}
