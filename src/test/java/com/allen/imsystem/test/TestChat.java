package com.allen.imsystem.test;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.FixBean;
import com.allen.imsystem.model.dto.ChatLastMessageTime;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.impl.RedisService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/springmvc.xml", "classpath:spring/applicationContext.xml"})
public class TestChat {

    @Autowired
    IChatService chatService;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    ChatMapper chatMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    RedisUtil redisUtil;
    @Test
    public void testChatList(){
        Long begin = System.currentTimeMillis();
        List<ChatSessionDTO> list = chatService.getChatList("28661270");
        System.out.println(list.size());
    }

    @Test
    public void getChatInfo(){
        ChatSessionInfo chatSessionInfo = chatService.getChatInfo(633786567424475138L,"28661270");
        System.out.println(chatSessionInfo);
    }

    @Test
    public void setAllHasRead(){
        chatService.setTalkAllMsgHasRead("28661270","633786567424475138");
    }

    @Test
    public void testMsgRecord(){
        List<MsgRecord> list = chatService.getMessageRecord("28661270","633786567424475138",new Date(),1,5);
        System.out.println(list.size());
        System.out.println(list);
    }

    @Test
    public void testOpenRemove(){
//        chatService.openNewPrivateChat("10547348","23456789");
        chatService.openNewPrivateChat("23456789","10547348");
//        chatService.removePrivateChat("23456789",635051814767476736L);
    }

    @Test
    public void type(){
//        chatService.openNewPrivateChat("10547348","23456789");
//        chatService.openNewPrivateChat("23456789","10547348");
//        chatService.removePrivateChat("23456789",635051814767476736L);
//        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,"634923715161669632",GlobalConst.ChatType.PRIVATE_CHAT);
//        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,"635051814767476736",GlobalConst.ChatType.PRIVATE_CHAT);
//        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,"633786567424475138",GlobalConst.ChatType.PRIVATE_CHAT);
//        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,"633786567424475137",GlobalConst.ChatType.PRIVATE_CHAT);
//        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,"633785817805881344",GlobalConst.ChatType.PRIVATE_CHAT);
    }


    @Test
    public void insert(){
        PrivateMsgRecord privateMsgRecord = new PrivateMsgRecord();
        privateMsgRecord.setMsgId(12345L);
        privateMsgRecord.setChatId(12345L);
        privateMsgRecord.setStatus(1);
        privateMsgRecord.setMsgType(1);
        privateMsgRecord.setContent("");
        privateMsgRecord.setFromUid("97554417");
        privateMsgRecord.setToUid("28661270");
        privateMsgRecord.setHasRead(false);
        chatMapper.insertPrivateMsgToRecord(privateMsgRecord);
    }

    @Test
    public void type1(){
        Integer type = (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_TYPE, "636342848935870464");
        System.out.println(type);
    }

    @Test
    public void wtf(){
        List<ChatLastMessageTime> list = chatMapper.selectChatSessionLastMsgTime();
        for(ChatLastMessageTime bean : list){

            Long chatId = bean.getChatId();
            Long timestamp = 0L;
            if(bean.getLastMsgTime() != null){
                timestamp = bean.getLastMsgTime().getTime();
            }
            if(!redisService.hHasKey(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME,chatId.toString())){
                redisService.hset(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME,chatId.toString(),timestamp.toString());
//                System.out.println(chatId);
            }
            System.out.println();
        }
    }


    @Test
    public void fix(){
        List<FixBean> fixBeanList = chatMapper.fixSelect();
        Integer affect = chatMapper.fix(fixBeanList);
        System.out.println(affect);
    }
}
