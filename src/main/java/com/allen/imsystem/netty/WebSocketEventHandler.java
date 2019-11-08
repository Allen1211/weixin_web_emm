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
public class WebSocketEventHandler implements MessageListener {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private GlobalChannelGroup channelGroup;

    @Autowired
    private INotifyService notifyService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

    }


    public void handleRequest(Integer eventCode, JSONObject data){
        switch (eventCode){
            case 101:{
                SendMsgDTO sendMsgDTO = data.getObject("data", SendMsgDTO.class);
                if(sendMsgDTO.getIsGroup()){
                    messageService.saveAndForwardGroupMessage(sendMsgDTO);
                }else{
                    messageService.saveAndForwardPrivateMessage(sendMsgDTO);
                }
                break;
            }
            case 102:{

            }
            case 104:{
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_APPLY_NOTIFY,uid);
                break;
            }
            case 105:{
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_FRIEND_NOTIFY,uid);
                break;
            }
        }
    }



    public void handleResponse(Integer eventCode,String destId, Object data){
        handleResponse(eventCode,destId,null,null,data);
    }

    public void handleResponse(Integer eventCode,String destId, Integer code, Object errMsg, Object data){
        switch (eventCode){
            case 201:
            case 202:
            case 205:
            case 204: {
                SocketResponse socketResponse = new SocketResponse(eventCode,1,data);
                channelGroup.send(destId,socketResponse);
                break;
            }
            case 203:{
                SocketResponse socketResponse = new SocketResponse(eventCode,0,code,errMsg,data);
                channelGroup.send(destId,socketResponse);
                break;
            }
        }
    }

    public void handleResponse(String destId,SocketResponse socketResponse){
        channelGroup.send(destId,socketResponse);
    }

    public void handleResponse(String destId, MultiDataSocketResponse socketResponse){
        channelGroup.send(destId,socketResponse);
    }

    public void handleMultiResponse(List<String> destId, SocketResponse socketResponse){

    }
}
