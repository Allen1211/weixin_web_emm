package com.allen.imsystem.message.service;

import com.allen.imsystem.message.service.impl.NotifyPackage;
import org.springframework.stereotype.Component;

@Component
public interface Subject {

    void registerObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObserver(NotifyPackage notifyPackage);
}
