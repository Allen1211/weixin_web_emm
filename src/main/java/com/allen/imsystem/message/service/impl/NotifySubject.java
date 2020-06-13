package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.service.Observer;
import com.allen.imsystem.message.service.Subject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotifySubject implements Subject {

    private Map<Integer, Observer> observerMap;

    public NotifySubject() {
        this.observerMap = new HashMap<>(2);
    }

    @Override
    public void registerObserver(Observer observer) {
        observerMap.put(observer.getType(),observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        Integer type = observer.getType();
        observerMap.remove(type,observer);
    }

    @Override
    public void notifyObserver(NotifyPackage notifyPackage) {
        Integer type = notifyPackage.getNotifyType();
        Observer observer = observerMap.get(type);
        observer.update(notifyPackage);
    }
}
