package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.chat.mappers.GroupChatMapper;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.bean.PageBean;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.file.model.MsgFileInfo;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.message.mappers.GroupMsgRecordMapper;
import com.allen.imsystem.message.mappers.PrivateMsgRecordMapper;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.service.MsgRecordService;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.allen.imsystem.common.Const.GlobalConst.*;

/**
 * @ClassName MsgRecordServiceImpl
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/16
 * @Version 1.0
 */
@Service
public class MsgRecordServiceImpl implements MsgRecordService {

    @Autowired
    private PrivateMsgRecordMapper privateMsgRecordMapper;

    @Autowired
    private GroupMsgRecordMapper groupMsgRecordMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    /**
     * 获取一个会话的聊天记录
     * @param uid 用户id
     * @param chatId 会话id
     * @param index 页码
     * @param pageSize 每页大小
     * @return 结果
     */
    @Override
    public Map<String, Object> getMessageRecord(boolean isGroup, String uid, Long chatId, Integer index, Integer pageSize) {
        Map<String, Object> resultMap = new HashMap<>(3);
        List<MsgRecord> messageList = null;
        // 如果是第一页，要获取一次总页数，记录一下统计的起始时间
        if (index == 1) {
            long now = System.currentTimeMillis();
            if (isGroup) {
                // 如果已经不是群成员了，只显示之前的聊天记录，不显示最新的
                GroupChat relation = groupChatMapper.findByChatId(chatId);
                if (!relation.getStatus()) {
                    now = relation.getUpdateTime().getTime();
                }
            }
            Integer totalSize = this.getAllHistoryMessageSize(isGroup, chatId, uid, new Date(now));
            int totalPage = 1;
            if (totalSize <= pageSize) {
                totalPage = 1;
            } else if (totalSize % pageSize == 0) {
                totalPage = totalSize / pageSize;
            } else {
                totalPage = totalSize / pageSize + 1;
            }
            messageList = doGetMessageList(isGroup, uid, chatId, null, index, pageSize);
            if (!CollectionUtils.isEmpty(messageList)) {
                Long latestMsgId = messageList.get(messageList.size() - 1).getMessageId();
                redisService.hset(RedisKey.KEY_RECORD_BEGIN_ID, chatId.toString(), latestMsgId.toString());
            }
            resultMap.put("messageList", messageList);
            resultMap.put("allPageSize", totalPage);
            resultMap.put("curPageIndex", index);
        } else {
            String beginMsgIdStr = (String) redisService.hget(RedisKey.KEY_RECORD_BEGIN_ID, chatId.toString());
            Long beginMsgId = null;
            if (StringUtils.isNotEmpty(beginMsgIdStr)) {
                beginMsgId = Long.parseLong(beginMsgIdStr);
            }
            messageList = doGetMessageList(isGroup, uid, chatId, beginMsgId, index, pageSize);
            resultMap.put("messageList", messageList);
            resultMap.put("curPageIndex", index);
        }

        return resultMap;
    }

    private List<MsgRecord> doGetMessageList(boolean isGroup, String uid, Long chatId, Long beginMsgId, Integer index, Integer pageSize) {

        PageBean pageBean = new PageBean(index, pageSize);
        List<MsgRecord> msgRecordList;

        if (isGroup) {
            String gid = groupChatService.getGidFromChatId(chatId,uid);
            msgRecordList = groupMsgRecordMapper.selectGroupChatHistoryMsg(gid, beginMsgId, uid, pageBean);
        } else {
            msgRecordList =
                    privateMsgRecordMapper.findMsgRecordList(chatId, beginMsgId, uid, pageBean);
        }

        if (msgRecordList == null) {
            return new ArrayList<>();
        }
        Long preMsgTime = null;
        for (int i = msgRecordList.size() - 1; i >= 0; i--) {
            MsgRecord msgRecord = msgRecordList.get(i);
            // 是否显示
            msgRecord.setShowMessage(true);

            // 获取发送者的用户信息
            UserInfoView userInfo = userService.findUserInfoDTO(msgRecord.getFromUid());
            msgRecord.setUserInfo(userInfo);

            if (msgRecord.getMessageType() != 4) {
                // 是否是自己发的
                msgRecord.setUserType(
                        uid.equals(msgRecord.getUserInfo().getUid()) ? 1 : 0
                );
            }

            // 消息类型
            switch (msgRecord.getMessageType()) {
                case 4: //群通知同普通文本
                case 1: {// 普通文本
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
                case 2: {// 图片
                    msgRecord.setMessageText("[图片]");
                    MsgFileInfo fileInfo = msgRecord.getFileInfo();
                    String imgUrl;
                    if (fileInfo == null) {
                        imgUrl = GlobalConst.Path.IMG_NOT_FOUND;
                    } else {
                        imgUrl = fileInfo.getDownloadUrl();
                    }
                    msgRecord.setMessageImgUrl(imgUrl == null ? GlobalConst.Path.IMG_NOT_FOUND : imgUrl);
                    break;
                }
                case 3: {// 文件
                    msgRecord.setMessageImgUrl(null);
                    MsgFileInfo fileInfo = msgRecord.getFileInfo();
                    if (fileInfo == null) {
                        msgRecord.setFileInfo(new MsgFileInfo("不存在的文件", ""));
                        msgRecord.getFileInfo().setFileSize("0");
                    } else {
                        String fileSize = FormatUtil.formatFileSize(fileInfo.getSize());
                        msgRecord.getFileInfo().setFileSize(fileSize);
                    }
                    break;
                }
            }

            // 发送的时间处理
            // 首条或者相差五分钟的才显示时间
            Long thisMsgTime = msgRecord.getMsgTimeDate().getTime();
            boolean showMsgTime = preMsgTime == null || (thisMsgTime - preMsgTime >= MAX_NOT_SHOW_TIME_SPACE);
            msgRecord.setShowMessageTime(showMsgTime);
            if (showMsgTime) {
                // 时间格式化处理
                String format = FormatUtil.formatMessageDate(msgRecord.getMsgTimeDate());
                msgRecord.setMessageTime(format);
            }
            preMsgTime = msgRecord.getMsgTimeDate().getTime();

        }
        Collections.reverse(msgRecordList);
        return msgRecordList;
    }


    /**
     * 获取某个会话所有聊天记录的条数
     * @param chatId 会话id
     * @param uid 用户id
     * @param beginTime 开始统计的时间
     * @return 聊天记录条数
     */
    @Override
    public Integer getAllHistoryMessageSize(boolean isGroup, Long chatId, String uid, Date beginTime) {
        Integer totalSize = null;
        if (isGroup) {
            String gid = groupChatService.getGidFromChatId(chatId, uid);
            totalSize = groupMsgRecordMapper.countAllGroupHistoryMsg(gid, beginTime);
        } else {
            totalSize = privateMsgRecordMapper.countAllPrivateHistoryMsg(chatId, beginTime);
        }
        return totalSize == null ? 0 : totalSize;
    }

    /**
     * 私聊消息入库
     * @param msg 发送过来的消息
     * @return 私聊消息实体类
     */
    @Override
    public PrivateMsgRecord savePrivateMsgRecord(SendMsgDTO msg) {
        PrivateMsgRecord privateMsgRecord = new PrivateMsgRecord();
        switch (msg.getMessageType()) {
            case 1: {    // 文字消息
                privateMsgRecord.setContent(msg.getMessageText());
                privateMsgRecord.setResourceUrl("");
                break;
            }
            case 2: {    // 图片消息
                privateMsgRecord.setContent("[图片]");
                privateMsgRecord.setResourceUrl(msg.getMessageImgUrl());
                String imgUrl = msg.getMessageImgUrl();
                if (StringUtils.isEmpty(imgUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                privateMsgRecord.setFileMd5(fileService.getMd5FromUrl(imgUrl));
                break;
            }
            case 3: {    // 文件消息
                privateMsgRecord.setContent(msg.getFileInfo().getFileName());
                privateMsgRecord.setResourceUrl(msg.getFileInfo().getDownloadUrl());
                String fileUrl = msg.getFileInfo().getDownloadUrl();
                if (StringUtils.isEmpty(fileUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                privateMsgRecord.setFileMd5(fileService.getMd5FromUrl(fileUrl));
                break;
            }
        }

        privateMsgRecord.setMsgId(msg.getMsgId());
        privateMsgRecord.setChatId(Long.valueOf(msg.getTalkId()));
        privateMsgRecord.setFromUid(msg.getSrcId());
        privateMsgRecord.setToUid(msg.getDestId());
        privateMsgRecord.setHasRead(false);
        privateMsgRecord.setMsgType(msg.getMessageType());
        privateMsgRecord.setStatus(true);
        Date msgTime = new Date(Long.parseLong(msg.getTimeStamp()));
        privateMsgRecord.setCreatedTime(msgTime);
        privateMsgRecord.setUpdateTime(msgTime);
        privateMsgRecordMapper.insert(privateMsgRecord);

        return privateMsgRecord;
    }

    /**
     * 保存群聊天记录
     * @param msg 发送过来的消息
     */
    @Override
    public GroupMsgRecord saveGroupChatMsgRecord(SendMsgDTO msg) {
        GroupMsgRecord groupMsgRecord = new GroupMsgRecord();
        groupMsgRecord.setMsgId(msg.getMsgId());
        Date createdTime = new Date(Long.parseLong(msg.getTimeStamp()));    // 以发送时间作为记录创建的时间
        groupMsgRecord.setCreatedTime(createdTime);
        groupMsgRecord.setUpdateTime(createdTime);
        groupMsgRecord.setGid(msg.getGid());
        groupMsgRecord.setStatus(true);
        groupMsgRecord.setSenderId(msg.getSrcId());
        groupMsgRecord.setMsgType(msg.getMessageType());

        switch (msg.getMessageType()) {
            case MsgType.GROUP_NOTIFY:
            case GlobalConst.MsgType.TEXT: {    // 文字消息
                groupMsgRecord.setContent(msg.getMessageText());
                break;
            }
            case GlobalConst.MsgType.IMAGE: {    // 图片消息
                groupMsgRecord.setContent("[图片]");
                String imgUrl = msg.getMessageImgUrl();
                if (StringUtils.isEmpty(imgUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(imgUrl));
                break;
            }
            case GlobalConst.MsgType.FILE: {    // 文件消息
                groupMsgRecord.setContent(msg.getFileInfo().getFileName());
                String fileUrl = msg.getFileInfo().getDownloadUrl();
                if (StringUtils.isEmpty(fileUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(fileUrl));
                break;
            }
        }
        // 插入新聊天记录
        groupMsgRecordMapper.insertNewGroupMsgRecord(groupMsgRecord);
        return groupMsgRecord;
    }


    @Override
    @Transactional
    public void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList) {
        if (msgRecordList != null && msgRecordList.size() > 0) {
            groupMsgRecordMapper.insertNewGroupMsgRecordBatch(msgRecordList);
        }
    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord) {
        groupMsgRecordMapper.insertNewGroupMsgRecord(groupMsgRecord);
    }
}
