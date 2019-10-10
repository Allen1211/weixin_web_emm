package com.allen.imsystem.common;

import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("defaultCacheHolder")
public class CacheHolder implements ICacheHolder {

    @Autowired
    private RedisUtil redisUtil;

    private static final Integer USER_LOGIN_EXPRIED = 60*60*3;

    @Override
    public String getUid(Object source) {
        String jwtToken = (String)source;
        String uid = JWTUtil.getMsgFromToken(jwtToken,"uid",String.class);
        return uid;
    }

    @Override
    public Integer getUserId(Object source) {
        String jwtToken = (String)source;
        Integer userId = JWTUtil.getMsgFromToken(jwtToken,"userId",Integer.class);
        return userId;
    }

    @Override
    public boolean setImageCode(String imageCode, String key) {
        if(imageCode != null && key != null){
            return redisUtil.set(key+"_image_code", imageCode, 5*60);
        }
        return false;
    }

    @Override
    public void removeImageCode(String key) {
        if(key != null){
            redisUtil.remove(key+"_image_code");
        }
    }

    @Override
    public boolean setEmailCode(String emailCode, String key) {
        if(emailCode != null && key != null){
            return redisUtil.set(key+"_email_code", emailCode, 20*60);
        }
        return false;
    }

    @Override
    public void removeEmailCode(String key) {
        if(key != null){
            redisUtil.remove(key+"_email_code");
        }
    }

    @Override
    public String getImageCode(String key) {
        if(key != null){
            return redisUtil.get(key+"_image_code");
        }
        return null;
    }

    @Override
    public String getEmailCode(String key) {
        if(key != null){
            return redisUtil.get(key+"_email_code");
        }
        return null;
    }
}
