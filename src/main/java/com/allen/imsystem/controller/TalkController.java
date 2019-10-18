package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.service.IChatService;
import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/talk")
@RestController
public class TalkController {

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @Autowired
    private IChatService chatService;

    @RequestMapping(value = "/getTalkData",method = RequestMethod.GET)
    public JSONResponse getTalkData(@RequestParam("talkId") String talkIdStr, HttpServletRequest request){
        Long talkId = Long.valueOf(talkIdStr);
        String uid = cacheHolder.getUid(request);
        ChatSessionInfo chatSessionInfo = chatService.getChatInfo(talkIdStr,uid);
        return new JSONResponse(1)
                .putData("isGroup",chatSessionInfo.getIsGroup())
                .putData("title",chatSessionInfo.getTitle())
                .putData("isGroupOwner",chatSessionInfo.getIsGroupOwner())
                .putData("talkId",chatSessionInfo.getTalkId());
    }

    @RequestMapping(value = "/getTalkList",method = RequestMethod.GET)
    public JSONResponse getTalkList(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        List<ChatSessionDTO> chatSessionDTOList = chatService.getChatList(uid);
        return new JSONResponse(1).putData("talkList",chatSessionDTOList);
    }

    @RequestMapping(value = "/setHasRead",method = RequestMethod.POST)
    public JSONResponse setHasRead(Map<String, Object> params,HttpServletRequest request){
        String talkId = (String) params.get("talkId");
        String uid = cacheHolder.getUid(request);
        chatService.setTalkAllMsgHasRead(uid,talkId);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/getMessageList",method = RequestMethod.GET)
    public JSONResponse getMessageList(Map<String,String>params,HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        String talkId = params.get("talkId");
        Integer index = Integer.valueOf(params.get("index"));
        Integer pageSize = Integer.valueOf(params.get("pageSize"));
        List<MsgRecord> result = chatService.getMessageRecord(uid,talkId,index,pageSize);
        return new JSONResponse(1).putData("messageList",request);
    }

}
