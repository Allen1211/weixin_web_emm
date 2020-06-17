package com.allen.imsystem.message.service;

import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MsgRecordService
 * @Description 与聊天记录有关的业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/6/16
 * @Version 1.0
 */
public interface MsgRecordService {

    /**
     * 获取一个会话的聊天记录
     * @param uid 用户id
     * @param chatId 会话id
     * @param index 页码
     * @param pageSize 每页大小
     * @return 结果
     */
    Map<String,Object> getMessageRecord(boolean isGroup, String uid, Long chatId, Integer index, Integer pageSize);

    /**
     * 获取某个会话所有聊天记录的条数
     * @param chatId 会话id
     * @param uid 用户id
     * @param beginTime 开始统计的时间
     * @return 聊天记录条数
     */
    Integer getAllHistoryMessageSize(boolean isGroup, Long chatId, String uid, Date beginTime);

    /**
     * 私聊消息入库
     * @param msg 发送过来的消息
     * @return 私聊消息实体类
     */
    PrivateMsgRecord savePrivateMsgRecord(SendMsgDTO msg);

    /**
     * 保存群聊天记录
     * @param msg 发送过来的消息
     */
    GroupMsgRecord saveGroupChatMsgRecord(SendMsgDTO msg);
    void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList);
    void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord);
}
