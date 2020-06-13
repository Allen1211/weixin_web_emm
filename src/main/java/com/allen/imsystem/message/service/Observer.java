package com.allen.imsystem.message.service;

import com.allen.imsystem.message.service.impl.NotifyPackage;

public interface Observer {

    Integer getType();

    void update(NotifyPackage notifyPackage);
}
