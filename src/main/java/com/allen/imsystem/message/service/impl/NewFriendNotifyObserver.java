package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.message.netty.WsEventHandler;
import com.allen.imsystem.message.service.Observer;
import com.allen.imsystem.message.service.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Set;


@Component
public class NewFriendNotifyObserver implements Observer {


    @Autowired
    private WsEventHandler wsEventHandler;

    private Integer type;

    public NewFriendNotifyObserver(ApplicationContext context) {
        Subject notifySubject = (Subject) context.getBean("notifySubject");
        this.type = 2;
        notifySubject.registerObserver(this);
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public void update(NotifyPackage notifyPackage) {
        Set<Object> destIdSet = notifyPackage.getDestIdSet();
        List notifyList = notifyPackage.getNotifyContentList();
        if(CollectionUtils.isEmpty(destIdSet) || CollectionUtils.isEmpty(notifyList)){
            return;
        }
        for(Object destId:destIdSet){
            wsEventHandler.handleResponse(GlobalConst.WsEvent.SERVER_PUSH_NEW_FRIEND_NOTIFY,(String)destId,notifyList);
        }
    }
}
