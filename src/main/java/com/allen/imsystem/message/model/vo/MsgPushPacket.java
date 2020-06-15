package com.allen.imsystem.message.model.vo;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MsgPushPacket
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class MsgPushPacket<K,V> {

    protected boolean needToPush;
    protected int eventCode;
    protected List<K> destIdList;
    protected List<V> pushMessageList;

    public MsgPushPacket() {
        this.needToPush = true;
    }

    public MsgPushPacket(int eventCode, List<K> destIdList, List<V> pushMessageList) {
        this.eventCode = eventCode;
        this.destIdList = destIdList;
        this.pushMessageList = pushMessageList;
    }

    public boolean isNeedToPush() {
        return needToPush;
    }

    public void setNeedToPush(boolean needToPush) {
        this.needToPush = needToPush;
    }

    public int getEventCode() {
        return eventCode;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public List<K> getDestIdList() {
        return destIdList;
    }

    public void setDestIdList(List<K> destIdList) {
        this.destIdList = destIdList;
    }

    public List<V> getPushMessageList() {
        return pushMessageList;
    }

    public void setPushMessageList(List<V> pushMessageList) {
        this.pushMessageList = pushMessageList;
    }
}
