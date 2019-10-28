package com.allen.imsystem.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TalkChannelGroup {
    private static ConcurrentHashMap<String, Channel> channelGroup = new ConcurrentHashMap<>(20);


    public static Channel getChannel(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        return channelGroup.get(key);
    }

    public static boolean addChannel(String key, Channel channel){
        if(StringUtils.isEmpty(key) || channel == null){
            return false;
        }
        channelGroup.put(key,channel);
        return true;
    }

    public static Channel removeChannel(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        return channelGroup.remove(key);
    }

    public static boolean send(String key,Object msg){
        if(StringUtils.isEmpty(key)){
            return false;
        }
        Channel channel = channelGroup.get(key);
        if(channel == null){
            return false;
        }
        TextWebSocketFrame frame = new TextWebSocketFrame(JSON.toJSONString(msg));
        channel.writeAndFlush(frame);
        return true;
    }

}
