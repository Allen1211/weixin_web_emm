package com.allen.imsystem.message.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName OneToOneMsgPacket
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OneToManySameMsgPushPacket<K,V> extends MsgPushPacket<K,V>{

    public OneToManySameMsgPushPacket(int eventCode, List<K> destIdList, V pushMessage) {
        super.destIdList = destIdList;
        super.pushMessageList = Collections.singletonList(pushMessage);
        super.eventCode = eventCode;
    }
}
