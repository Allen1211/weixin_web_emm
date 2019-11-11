package com.allen.imsystem.common;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 从request的attribute中获取用户信息 （验证token后添加)
 */
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
