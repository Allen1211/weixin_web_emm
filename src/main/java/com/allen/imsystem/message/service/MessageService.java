package com.allen.imsystem.message.service;

import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 消息接收与转发推送相关的业务逻辑接口
 */
@Service
public interface MessageService {


    void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO);

    void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO);

    void sendGroupNotify(String destId, String gid, List<GroupMsgRecord> notifyList);

    void sendGroupNotify(Set<Object> destIdList, String gid, List<GroupMsgRecord> notifyList);

    void sendGroupNotify(Set<Object> destIdList, String gid, GroupMsgRecord notify);

    void handleSendFail(SendMsgDTO sendMsgDTO, String content);

    void sendServerAck(SendMsgDTO sendMsgDTO, Long msgId, Long chatId);
}
