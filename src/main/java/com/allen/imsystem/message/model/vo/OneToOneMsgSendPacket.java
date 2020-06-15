package com.allen.imsystem.message.model.vo;

import com.allen.imsystem.common.utils.MutableSingletonList;


/**
 * @ClassName OneToOneMsgSendPacket
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class OneToOneMsgSendPacket<K,V> extends MsgSendPacket<K,V>{

    public OneToOneMsgSendPacket() {
    }

    public OneToOneMsgSendPacket(K srcId,V sendMessage) {
        super(srcId, new MutableSingletonList<>(sendMessage));
    }

    public K getSrcId(){
        return super.getSrcId();
    }

    public V getSendMessage(){
        if(super.getSendMessageList() != null && super.getSendMessageList().size() > 0){
            return super.getSendMessageList().get(0);
        }else{
            return null;
        }
    }

    public void setSrcId(K srcId){
        super.setSrcId(srcId);
    }

    public void setSendMessage(V sendMessage){
        super.getSendMessageList().set(0, sendMessage);
    }

}
