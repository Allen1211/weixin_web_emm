package com.allen.imsystem.test.chat;

import com.allen.imsystem.common.utils.ChatIdUtil;
import com.allen.imsystem.mappers.ChatMapper;
import com.allen.imsystem.mappers.GroupChatMapper;
import com.allen.imsystem.mappers.PrivateChatMapper;
import com.allen.imsystem.mappers.PrivateMsgRecordMapper;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.model.pojo.UserChatGroup;
import com.allen.imsystem.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName TestChat
 * @Description
 * @Author XianChuLun
 * @Date 2020/5/25
 * @Version 1.0
 */
public class TestChat extends BaseTest {

    @Autowired
    ChatMapper chatMapper;

    @Autowired
    PrivateChatMapper privateChatMapper;

    @Autowired
    PrivateMsgRecordMapper privateMsgRecordMapper;

    @Autowired
    GroupChatMapper groupChatMapper;

//    @Test
//    public void changePrivateChatId(){
//        List<PrivateChat> privateChatList = chatMapper.selectAllPrivateChat();
//        for(PrivateChat privateChat : privateChatList){
//            long newChatId = ChatIdUtil.generate(0);
//            long oldChatId = privateChat.getChatId();
//            privateChat.setChatId(newChatId);
//            int affect = chatMapper.updatePrivateChatId(oldChatId,newChatId);
//            System.out.println(affect);
//            affect = chatMapper.updatePriMsgRecordChatId(oldChatId,newChatId);
//            System.out.println(affect);
//        }
//    }
//
//    @Test
//    public void changeGroupChatId(){
//        List<UserChatGroup> userChatGroupList = groupChatMapper.selectAll();
//        for(UserChatGroup userChatGroup : userChatGroupList){
//            long newChatId = ChatIdUtil.generate(1);
//            long oldChatId = userChatGroup.getChatId();
//            userChatGroup.setChatId(newChatId);
//            int affect = groupChatMapper.updateUserGroupChatId(oldChatId,newChatId);
//            System.out.println(affect);
//        }
//    }

//    @Test
//    public void fillPrivateChatLastMsg(){
//        List<PrivateChat> privateChatList = chatMapper.selectAllPrivateChat();
//        for(PrivateChat privateChat : privateChatList){
//            if(privateChat.getLastMsgId() != null){
//                PrivateMsgRecord privateMsgRecord = privateMsgRecordMapper.selectById(privateChat.getLastMsgId());
//                if(privateMsgRecord != null){
//                    System.out.println(privateMsgRecord);
//                    privateChat.setLastMsgContent(privateMsgRecord.getContent());
//                    privateChat.setLastMsgCreateTime(privateMsgRecord.getCreatedTime());
//                    privateChatMapper.update(privateChat);
//                }
//            }
//        }
//    }
}
