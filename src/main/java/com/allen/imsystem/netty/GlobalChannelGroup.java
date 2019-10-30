package com.allen.imsystem.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalChannelGroup {
    private static ConcurrentHashMap<String, Set<Channel>> channelGroup = new ConcurrentHashMap<>(20);

//    public static Channel getChannel(String key){
//        if(StringUtils.isEmpty(key)){
//            return null;
//        }
//        Set<Channel> channelSet = channelGroup.get(key);
//        if(channelSet == null){
//            return null;
//        }
//
//        return channelSet.get(key);
//    }

    public static boolean addChannel(String key, Channel channel){
        if(StringUtils.isEmpty(key) || channel == null){
            return false;
        }
        Set<Channel> channelSet = channelGroup.get(key);
        if(channelSet == null){
            channelSet = new HashSet<>(2);
            channelGroup.put(key,channelSet);
        }
        return channelSet.add(channel);

    }

    public static Set<Channel> removeAllChannel(String key){
        if(!StringUtils.isEmpty(key)){
            return channelGroup.remove(key);
        }
        return null;
    }

    public static Boolean removeChannel(String key, Channel channel){
        if(!StringUtils.isEmpty(key) && channel!=null){
            Set<Channel> channelSet = channelGroup.get(key);
            if(channelSet!=null){
                return channelSet.remove(channel);
            }
        }
        return false;
    }

    public static boolean hasChannel(String key){
        if(StringUtils.isEmpty(key)){
            return false;
        }
        Set<Channel> channelSet = channelGroup.get(key);
        return channelSet!=null && !channelSet.isEmpty();
    }

    public static boolean send(String key,Object msg){
        if(StringUtils.isEmpty(key) || msg==null){
            return false;
        }
        Set<Channel> channelSet = channelGroup.get(key);
        if(channelSet == null){
            return false;
        }
        String jsonMsg = JSON.toJSONString(msg);
        for(Channel channel : channelSet){
            TextWebSocketFrame frame = new TextWebSocketFrame(jsonMsg);
            channel.writeAndFlush(frame);
        }
        return true;
    }
}
