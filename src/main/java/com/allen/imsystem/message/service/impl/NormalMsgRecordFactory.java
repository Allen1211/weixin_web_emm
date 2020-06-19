package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.file.model.MsgFileInfo;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.common.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NormalMsgRecordFactory extends MsgRecordFactory {

    @Autowired
    private ChatService chatService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;


    @Override
    public MsgRecord packMsgRecord(SendMsgDTO sendMsgDTO) {

        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());

        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        msgRecord.setMessageType(sendMsgDTO.getMessageType());

        switch (msgRecord.getMessageType()) {
            case GlobalConst.MsgType.TEXT: {
                msgRecord.setMessageText(sendMsgDTO.getMessageText());
                break;
            }
            case GlobalConst.MsgType.IMAGE: {
                msgRecord.setMessageText("[图片]");
                msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
                break;
            }
            case GlobalConst.MsgType.FILE: {
                MsgFileInfo fileInfo = sendMsgDTO.getFileInfo();
                String md5 = fileService.getMd5FromUrl(fileInfo.getDownloadUrl());
                String sizeStr = (String) redisService.get(md5);
                Long size = sizeStr == null ? 0L : Long.parseLong(sizeStr);
                String fileSize = FormatUtil.formatFileSize(size);
                fileInfo.setFileSize(fileSize);
                fileInfo.setSize(size);
                msgRecord.setFileInfo(fileInfo);
                // 文件消息内容为文件名
                msgRecord.setMessageText(fileInfo.getFileName());
                break;
            }
        }

        // 4.1发送者信息
        UserInfoView userInfo = userMapper.selectUserInfoDTO(sendMsgDTO.getSrcId());
        msgRecord.setUserInfo(userInfo);
        // 4.2消息时间
        long msgSendTimestamp = Long.parseLong(sendMsgDTO.getTimeStamp());
        Date msgTimeDate = new Date(msgSendTimestamp);
        String msgTimeStr = FormatUtil.formatMessageDate(msgTimeDate);
        msgRecord.setMsgTimeDate(msgTimeDate);
        msgRecord.setMessageTime(msgTimeStr);
//        // 4.3是否显示时间 消息发送时间 - 会话上一条消息发送时间 > 固定时间 即显示时间
//        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(chatId);
//        boolean showTime = msgSendTimestamp - chatLastMsgTime > GlobalConst.MAX_NOT_SHOW_TIME_SPACE;
//        msgRecord.setShowMessageTime(showTime);

        return msgRecord;
    }


}