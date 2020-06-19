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

    /**
     * 保存并转发私聊消息
     * @param sendMsgDTO 客户端发送的消息
     */
    void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO);

    /**
     * 保存并转发群聊消息
     * @param sendMsgDTO 客户端发送的消息
     */
    void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO);

    /**
     * 发送群通知
     * @param destId 消息目标用户id
     * @param gid 群id
     * @param notifyList 通知列表
     */
    void sendGroupNotify(String destId, String gid, List<GroupMsgRecord> notifyList);

    /**
     * 发送群通知
     * @param destIdList 消息目标用户id列表
     * @param gid 群id
     * @param notifyList 通知列表
     */
    void sendGroupNotify(Set<String> destIdList, String gid, List<GroupMsgRecord> notifyList);

    /**
     * 发送群通知
     * @param destIdList 消息目标用户id列表
     * @param gid 群id
     * @param notify 通知
     */
    void sendGroupNotify(Set<String> destIdList, String gid, GroupMsgRecord notify);

    /**
     * 发送群通知
     * @param destIdList 消息目标用户id列表
     * @param gid 群id
     * @param notify 通知
     */
    void sendGroupNotify(List<String> destIdList, String gid, GroupMsgRecord notify);

    /**
     * 发送失败报文
     * @param sendMsgDTO 客户端发送的消息
     * @param content 内容
     */
    void handleSendFail(SendMsgDTO sendMsgDTO, String content);

    /**
     * 发送服务端已读回执
     * @param sendMsgDTO 客户端发送的消息
     * @param msgId 已读的消息id
     * @param chatId 会话id
     */
    void sendServerAck(SendMsgDTO sendMsgDTO, Long msgId, Long chatId);
}
