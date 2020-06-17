package com.allen.imsystem.message.netty;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.netty.bean.MultiDataSocketResponse;
import com.allen.imsystem.message.netty.bean.SocketResponse;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.message.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class WsEventHandler implements MessageListener {

    @Autowired
    private MessageService messageService;

    @Autowired
    private GlobalChannelGroup channelGroup;

    @Autowired
    private NotifyService notifyService;

    @Override
    public void onMessage(Message message, byte[] pattern) {

    }


    public void handleClientSend(int eventCode, JSONObject data) {
        log.info("收到客户端报文：" + data);
        switch (eventCode) {
            case GlobalConst.WsEvent.CLIENT_SEND_MSG: {
                SendMsgDTO sendMsgDTO = data.getObject("data", SendMsgDTO.class);
                if (sendMsgDTO.getIsGroup()) {
                    messageService.saveAndForwardGroupMessage(sendMsgDTO);
                } else {
                    messageService.saveAndForwardPrivateMessage(sendMsgDTO);
                }
                break;
            }
            case GlobalConst.WsEvent.CLIENT_MSG_ACK: {

            }
            case GlobalConst.WsEvent.CLIENT_NEW_APPLY_NOTIFY_ACK: {
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_APPLY_NOTIFY, uid);
                break;
            }
            case GlobalConst.WsEvent.CLIENT_NEW_FRIEND_NOTIFY_ACK: {
                String uid = data.getString("uid");
                notifyService.deleteAllNotify(GlobalConst.NotifyType.NEW_FRIEND_NOTIFY, uid);
                break;
            }
        }
    }

    public void handleServerPush(Integer eventCode, String destId, Integer code, Object errMsg, Object data) {
        switch (eventCode) {
            case GlobalConst.WsEvent.SERVER_PUSH_MSG:
            case GlobalConst.WsEvent.SERVER_MSG_ACK_SUCCESS:
            case GlobalConst.WsEvent.SERVER_PUSH_NEW_APPLY_NOTIFY:
            case GlobalConst.WsEvent.SERVER_PUSH_NEW_FRIEND_NOTIFY: {
                SocketResponse socketResponse = new SocketResponse(eventCode, 1, data);
                channelGroup.send(destId, socketResponse);
                break;
            }
            case GlobalConst.WsEvent.SERVER_MSG_ACK_FAIL: {
                SocketResponse socketResponse = new SocketResponse(eventCode, 0, code, errMsg, data);
                channelGroup.send(destId, socketResponse);
                break;
            }
        }
    }

    public void handleServerPush(Integer eventCode, String destId, Object data) {
        handleServerPush(eventCode, destId, null, null, data);
    }

    public <K,V> void handleServerPush(Integer eventCode, List<K> destIdList, List<V> dataList) {
        if (CollectionUtils.isEmpty(destIdList) || dataList == null) {
            return;
        }
        for (int i = 0; i < destIdList.size(); i++) {
            handleServerPush(eventCode, (String) destIdList.get(i), null, null, dataList.get(i));
        }
    }

    public void handleServerPush(String destId, SocketResponse socketResponse) {
        channelGroup.send(destId, socketResponse);
    }

    public void handleServerPush(String destId, MultiDataSocketResponse socketResponse) {
        channelGroup.send(destId, socketResponse);
    }

}
