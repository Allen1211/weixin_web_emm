package com.allen.imsystem.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class GlobalChannelGroup {
    private ConcurrentHashMap<String, Set<Channel>> channelGroup;


    public GlobalChannelGroup(){
        System.out.println("load");
        channelGroup = new ConcurrentHashMap<>(20);
    }
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

    public boolean addChannel(String key, Channel channel){
        System.out.println("add channel to container:" + channelGroup.hashCode());
        if(StringUtils.isEmpty(key) || channel == null){
            return false;
        }
        Set<Channel> channelSet = channelGroup.get(key);
        if(channelSet == null){
            channelSet = new HashSet<>(2);
            channelGroup.put(key,channelSet);
        }
        channelSet.add(channel);
        return channelGroup.put(key,channelSet)!=null;
    }

    public Set<Channel> removeAllChannel(String key){
        if(!StringUtils.isEmpty(key)){
            return channelGroup.remove(key);
        }
        return null;
    }

    public Boolean removeChannel(String key, Channel channel){
        if(!StringUtils.isEmpty(key) && channel!=null){
            Set<Channel> channelSet = channelGroup.get(key);
            if(channelSet!=null){
                return channelSet.remove(channel);
            }
        }
        return false;
    }

    public boolean hasChannel(String key){
        if(StringUtils.isEmpty(key)){
            return false;
        }
        Set<Channel> channelSet = channelGroup.get(key);
        return channelSet!=null && !channelSet.isEmpty();
    }

    public boolean send(String key,Object msg){
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
