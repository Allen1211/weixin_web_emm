package com.allen.imsystem.test;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.service.IChatService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/springmvc.xml", "classpath:spring/applicationContext.xml"})
public class TestChat {

    @Autowired
    IChatService chatService;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Test
    public void testChatList(){
        Long begin = System.currentTimeMillis();
        List<ChatSessionDTO> list = chatService.getChatList("28661270");
        Long end = System.currentTimeMillis();
        System.out.println((end-begin));
        Assert.assertNotNull(list);
        System.out.println(list.size());
    }

    @Test
    public void re(){
    }
}
