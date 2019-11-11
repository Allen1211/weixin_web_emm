package com.allen.imsystem.netty;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.model.dto.MultiDataSocketResponse;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.dto.SocketResponse;
import com.allen.imsystem.service.IMessageService;
import com.allen.imsystem.service.INotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WsEventHandler implements MessageListener {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private GlobalChannelGroup channelGroup;

    @Autowired
    private INotifyService notifyService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

    }


    public void handleClientSend(int eventCode, JSONObject data){
        switch (eventCode){
            case GlobalConst.WsEvent.CLIENT_SEND_MSG:{
                SendMsgDTO sendMsgDTO = data.getObject("data", SendMsgDTO.class);
                if(sendMsgDTO.getIsGroup()){
                    messageService.saveAndForwardGroupMessage(sendMsgDTO);
                }else{
                    messageService.saveAndForwardPrivateMessage(sendMsgDTO);
                }
                break;
            }
            case GlobalConst.WsEvent.CLIENT_MSG_ACK:{

            }
            case GlobalConst.WsEvent.CLIENT_NEW_APPLY_NOTIFY_ACK:{
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_APPLY_NOTIFY,uid);
                break;
            }
            case GlobalConst.WsEvent.CLIENT_NEW_FRIEND_NOTIFY_ACK:{
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_FRIEND_NOTIFY,uid);
                break;
            }
        }
    }

    public void handleResponse(Integer eventCode,String destId, Integer code, Object errMsg, Object data){
        switch (eventCode){
            case GlobalConst.WsEvent.SERVER_PUSH_MSG:
            case GlobalConst.WsEvent.SERVER_MSG_ACK_SUCCESS:
            case GlobalConst.WsEvent.SERVER_PUSH_NEW_APPLY_NOTIFY:
            case GlobalConst.WsEvent.SERVER_PUSH_NEW_FRIEND_NOTIFY: {
                SocketResponse socketResponse = new SocketResponse(eventCode,1,data);
                channelGroup.send(destId,socketResponse);
                break;
            }
            case GlobalConst.WsEvent.SERVER_MSG_ACK_FAIL:{
                SocketResponse socketResponse = new SocketResponse(eventCode,0,code,errMsg,data);
                channelGroup.send(destId,socketResponse);
                break;
            }
        }
    }

    public void handleResponse(Integer eventCode,String destId, Object data){
        handleResponse(eventCode,destId,null,null,data);
    }

    public void handleResponse(String destId,SocketResponse socketResponse){
        channelGroup.send(destId,socketResponse);
    }

    public void handleResponse(String destId, MultiDataSocketResponse socketResponse){
        channelGroup.send(destId,socketResponse);
    }

}
