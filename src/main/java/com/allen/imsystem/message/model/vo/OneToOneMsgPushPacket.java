package com.allen.imsystem.message.model.vo;

import com.allen.imsystem.common.utils.MutableSingletonList;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName OneToOneMsgPacket
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class OneToOneMsgPushPacket<K,V> extends MsgPushPacket<K,V>{

    public OneToOneMsgPushPacket(int eventCode, K destId, V pushMessage) {
//        super.destIdList = Stream.of(destId).collect(Collectors.toList());
        super.destIdList = new MutableSingletonList<>(destId);
        super.pushMessageList = Stream.of(pushMessage).collect(Collectors.toList());
        super.eventCode = eventCode;
    }

    public K getDestId() {
        return super.getDestIdList().get(0);
    }

    public void setDestId(K destId) {
        super.destIdList.set(0, destId);
    }

    public V getPushMessage(){
        return super.pushMessageList.get(0);
    }

    public void setPushMessage(V pushMessage){
        super.pushMessageList.set(0, pushMessage);
    }
}
