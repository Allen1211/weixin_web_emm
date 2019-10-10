package com.allen.imsystem.common.utils;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.pojo.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HTTPUtil {

    public static String getUidFromSession(HttpSession session){
        return getUserFromSession(session).getUid();
    }

    public static Integer getUserIdFromSession(HttpSession session){
        return getUserFromSession(session).getId();
    }

    public static User getUserFromSession(HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user==null)
            throw new BusinessException(ExceptionType.NO_LOGIN_ERROR);
        return user;
    }

    public static String getTokenFromHeader(HttpServletRequest request){
        String token = request.getHeader("token");
        if(token == null){
            throw new BusinessException(ExceptionType.TOKEN_EXPIRED_ERROR,"无法获取token，token不存在");
        }
        return token;
    }
}
