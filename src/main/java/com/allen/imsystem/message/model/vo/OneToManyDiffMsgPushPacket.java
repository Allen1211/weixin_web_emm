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
public class OneToManyDiffMsgPushPacket<K,V> extends MsgPushPacket<K,V>{

    public OneToManyDiffMsgPushPacket() {
        super();
    }

    public OneToManyDiffMsgPushPacket(List<K> destIdList){
        super();
        super.destIdList = destIdList;
    }

    public OneToManyDiffMsgPushPacket(int eventCode, List<K> destIdList, List<V> pushMessageList) {
        super();
        super.destIdList = destIdList;
        super.pushMessageList = pushMessageList;
        super.eventCode = eventCode;
    }


}
