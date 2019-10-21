package com.allen.imsystem.controller.websocket;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.model.dto.MultiDataSocketResponse;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.dto.ServerAckDTO;
import com.allen.imsystem.model.dto.SocketResponse;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IMessageService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEventHandler implements MessageListener {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IChatService chatService;

    @Autowired
    private IUserService userService;

    @Autowired
    @Qualifier("AttrCacheHolder")
    private ICacheHolder cacheHolder;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    public void handleRequest(Integer eventCode, JSONObject data){
        switch (eventCode){
            case 101:{
                SendMsgDTO sendMsgDTO = data.getObject("data", SendMsgDTO.class);
                messageService.sendPrivateMessage(sendMsgDTO);
                break;
            }
            case 102:{

            }
        }
    }



    public void handleResponse(Integer eventCode,String destId, Object data){
        handleResponse(eventCode,destId,null,null,data);
    }

    public void handleResponse(Integer eventCode,String destId, Integer code, Object errMsg, Object data){
        switch (eventCode){
            case 201:
            case 202: {
                SocketResponse socketResponse = new SocketResponse(eventCode,1,data);
                TalkWebSocket.pushMessageToClient(destId,socketResponse);
                break;
            }
            case 203:{
                SocketResponse socketResponse = new SocketResponse(eventCode,0,code,errMsg,data);
                TalkWebSocket.pushMessageToClient(destId,socketResponse);
                break;
            }
        }
    }

    public void handleResponse(String destId,SocketResponse socketResponse){
        TalkWebSocket.pushMessageToClient(destId,socketResponse);
    }

    public void handleResponse(String destId, MultiDataSocketResponse socketResponse){
        TalkWebSocket.pushMessageToClient(destId,socketResponse);
    }
}
