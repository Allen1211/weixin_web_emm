package com.allen.imsystem.common.message;

public interface Observer {

    Integer getType();

    void update(NotifyPackage notifyPackage);
}
