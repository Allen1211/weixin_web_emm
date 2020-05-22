package com.allen.imsystem.common.message;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.netty.WsEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Component
//@Lazy
public class NewApplyNotifyObserver implements Observer {

    @Autowired
    private WsEventHandler wsEventHandler;

    private Integer type;

    @Autowired
    public NewApplyNotifyObserver(ApplicationContext context) {
        Subject notifySubject = (Subject) context.getBean("notifySubject");
        this.type = 1;
        notifySubject.registerObserver(this);
        System.out.println(notifySubject);
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public void update(NotifyPackage notifyPackage) {
        Set<Object> destIdSet = notifyPackage.getDestIdSet();
        List notifyList = notifyPackage.getNotifyContentList();
        if (CollectionUtils.isEmpty(destIdSet) || CollectionUtils.isEmpty(notifyList)) {
            return;
        }
        for (Object destId : destIdSet) {
            wsEventHandler.handleResponse(GlobalConst.WsEvent.SERVER_PUSH_NEW_APPLY_NOTIFY,
                    (String) destId, notifyList);
        }
    }
}
