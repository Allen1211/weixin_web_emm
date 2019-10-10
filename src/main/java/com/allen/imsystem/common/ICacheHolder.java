package com.allen.imsystem.common;

import org.springframework.stereotype.Component;

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
