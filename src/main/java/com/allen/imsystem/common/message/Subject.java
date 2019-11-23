package com.allen.imsystem.common.message;

import org.springframework.stereotype.Component;

@Component
public interface Subject {

    void registerObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObserver(NotifyPackage notifyPackage);
}
