package com.allen.imsystem.controller.websocket;


import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.service.impl.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class TalkWebSocket extends AbstractWebSocketHandler {

    @Autowired
    private RedisService redisService;

    @Autowired
    private WebSocketEventHandler webSocketEventHandler;

    private static Map<String, WebSocketSession> sessionPool = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Map<String, Object> attributes = session.getAttributes();
        String uid = (String) attributes.get("uid");
        sessionPool.put(uid, session);
        System.out.println(new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(new Date())
                + " " + uid + " open talk websocket");
        redisService.hset(GlobalConst.Redis.KEY_USER_STATUS,uid,1);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("getMessage: " + message.getPayload());
        // 1、获取客户端发送过来的数据
        String payLoad = message.getPayload();
        // 2、JSON解析出eventCode
        JSONObject jsonObject = JSONObject.parseObject(payLoad);
        Integer eventCode = (Integer) jsonObject.get("eventCode");

        // 3、委托给处理类处理请求
        new Thread(()->{
            webSocketEventHandler.handleRequest(eventCode,jsonObject);
        }).start();

    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

    }


    @Override
    /**
     * 连接关闭
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String uid = (String) session.getAttributes().get("uid");
        System.out.println(new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(new Date())+
                " websocket client closed : " + uid);
        if(uid != null){
            sessionPool.remove(uid);
            // TODO redis在线状态设为离线
            redisService.hset(GlobalConst.Redis.KEY_USER_STATUS,uid,0);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }


    public static boolean pushMessageToClient(String destId, Object socketResponse)  {
        WebSocketSession session = sessionPool.get(destId);
        if(session != null && session.isOpen()){
            String jsonResponse = toJSONString(socketResponse);
            TextMessage textMessage = new TextMessage(jsonResponse);
            try{
                session.sendMessage(textMessage);
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    private Integer getEventCodeFromJson(String json){
        JSONObject jsonObject = JSONObject.parseObject(json);
        Integer eventCode = (Integer) jsonObject.get("eventCode");
        return eventCode;
    }

    private static String toJSONString(Object object){
        if(object == null){
            return null;
        }
        return JSONObject.toJSONString(object);
    }
}
