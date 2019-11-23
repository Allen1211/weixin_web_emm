package com.allen.imsystem.common;

import org.springframework.stereotype.Component;

/**
 * 从缓存中获取用一些信息的方法。具体实现可以由session，redis，cookie等实现
 */
@Component
public interface ICacheHolder {

    String getUid(Object source);

    Integer getUserId(Object source);

    boolean setImageCode(String imageCode, String key);

    void removeImageCode(String key);

    boolean setEmailCode(String emailCode, String key);

    void removeEmailCode(String key);

    String getImageCode(String key);

    String getEmailCode(String key);
}
