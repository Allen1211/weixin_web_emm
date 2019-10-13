package com.allen.imsystem.common;

import com.allen.imsystem.common.utils.JWTUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component("AttrCacheHolder")
public class CacheHolderAttr extends CacheHolder {

    @Override
    public String getUid(Object source) {
        HttpServletRequest request = (HttpServletRequest)source;
        String uid = (String) request.getAttribute("uid");
        return uid;
    }

    @Override
    public Integer getUserId(Object source) {
        HttpServletRequest request = (HttpServletRequest)source;
        Integer userId = (Integer) request.getAttribute("userId");
        return userId;
    }
}
