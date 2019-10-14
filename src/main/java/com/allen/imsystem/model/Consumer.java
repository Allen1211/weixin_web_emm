package com.allen.imsystem.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class Consumer implements MessageListener {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private RedisSerializer<String > serializer;


    private RedisSerializer getSerializer(){
        if(serializer == null){
            this.serializer = redisTemplate.getStringSerializer();
        }
        return serializer;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<String> serializer = getSerializer();
        String msg = serializer.deserialize(message.getBody());
        String channel = serializer.deserialize(message.getChannel());
        String patternStr = serializer.deserialize(pattern);
        System.out.println("msg->"+msg);
        System.out.println("channel->"+channel);
        System.out.println("pattern->"+patternStr);
    }
}
