package com.allen.imsystem.common.utils;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.pojo.User;

import javax.servlet.http.HttpSession;

public class SessionUtil {

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
}
