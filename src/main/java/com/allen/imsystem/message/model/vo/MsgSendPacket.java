package com.allen.imsystem.message.model.vo;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName MsgSendPacket
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class MsgSendPacket<K,V> {
    private boolean needAck;
    private K srcId;
    private List<V> sendMessageList;

    public MsgSendPacket() {
        this.needAck = true;
    }

    public MsgSendPacket(boolean needAck) {
        this.needAck = true;
        this.needAck = needAck;
    }

    public MsgSendPacket(K srcId, List<V> sendMessageList) {
        this.needAck = true;
        this.srcId = srcId;
        this.sendMessageList = sendMessageList;
    }

    public K getSrcId() {
        return srcId;
    }

    public void setSrcId(K srcId) {
        this.srcId = srcId;
    }

    public List<V> getSendMessageList() {
        return sendMessageList;
    }

    public void setSendMessageList(List<V> sendMessageList) {
        this.sendMessageList = sendMessageList;
    }

    public boolean isNeedAck() {
        return needAck;
    }

    public void setNeedAck(boolean needAck) {
        this.needAck = needAck;
    }
}
