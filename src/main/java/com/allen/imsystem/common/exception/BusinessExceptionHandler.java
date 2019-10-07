package com.allen.imsystem.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.model.dto.JSONResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义业务异常统一处理器
 */
@Deprecated
public class BusinessExceptionHandler implements HandlerExceptionResolver {


    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        JSONResponse jsonResponse = new JSONResponse();
        if(e instanceof BusinessException){
            BusinessException be = (BusinessException) e;
            jsonResponse.setStatus(0);
            jsonResponse.setCode(be.getCode());
            if(be.getErrMsg() == null){
                jsonResponse.setErrMsg(be.getMessage());
            }else{
                jsonResponse.setErrMsg(be.getErrMsg());
            }
        } else{
            jsonResponse.setStatus(0);
            jsonResponse.setCode(1000);
            jsonResponse.setErrMsg(e.getMessage());
        }
        try {
            httpServletResponse.setStatus(400);
            httpServletResponse.setContentType("application/json; charset=UTF-8");
            httpServletResponse.getWriter().write(JSONObject.toJSONString(jsonResponse));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
