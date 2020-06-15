package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.*;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Scope("prototype")
public class MsgOnlineFilterHandler extends MsgHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket) {

        List<PushMessageDTO> pushMessageList = msgPushPacket.getPushMessageList();
        List<String> destIdList = msgPushPacket.getDestIdList();

        // 至少有一个推送对象时才需要执行下面的处理器
        if(CollectionUtils.isEmpty(destIdList)){
            return;
        }

        List<String> onlineDestIdList = null;

        boolean hasSomeoneOnline = false;

        for (String destId : destIdList) {
            if (userService.isOnline(destId)) {
                if (!hasSomeoneOnline) {
                    hasSomeoneOnline = true;
                    // 惰性实例化，避免无谓的内存浪费
                    onlineDestIdList = new ArrayList<>();
                    if(pushMessageList == null){
                        pushMessageList = new ArrayList<>();
                        msgPushPacket.setPushMessageList(pushMessageList);
                    }
                }
                onlineDestIdList.add(destId);
                pushMessageList.add(new PushMessageDTO());
            }
        }
        msgPushPacket.setNeedToPush(hasSomeoneOnline);
        // 至少有一个推送对象在线时才需要执行下面的处理器
        if(hasSomeoneOnline){
            msgPushPacket.setDestIdList(onlineDestIdList);
            if(nextHandler != null){
                nextHandler.handleMsg(msgSendPacket,msgPushPacket);
            }
        }else{
            msgPushPacket.setDestIdList(Collections.emptyList());
        }



    }
}
