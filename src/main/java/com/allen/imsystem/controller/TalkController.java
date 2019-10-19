package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.IChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
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

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "/getTalkData", method = RequestMethod.GET)
    public JSONResponse getTalkData(@RequestParam("talkId") String talkIdStr, HttpServletRequest request) {
        Long talkId = Long.valueOf(talkIdStr);
        String uid = cacheHolder.getUid(request);
        ChatSessionInfo chatSessionInfo = chatService.getChatInfo(talkIdStr, uid);
        return new JSONResponse(1)
                .putData("isGroup", chatSessionInfo.getIsGroup())
                .putData("title", chatSessionInfo.getTitle())
                .putData("isGroupOwner", chatSessionInfo.getIsGroupOwner())
                .putData("talkId", chatSessionInfo.getTalkId().toString());
    }

    @RequestMapping(value = "/getTalkList", method = RequestMethod.GET)
    public JSONResponse getTalkList(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        List<ChatSessionDTO> chatSessionDTOList = chatService.getChatList(uid);
        return new JSONResponse(1).putData("talkList", chatSessionDTOList);
    }

    @RequestMapping(value = "/setHasRead", method = RequestMethod.POST)
    public JSONResponse setHasRead(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String talkId = params.get("talkId");
        String uid = cacheHolder.getUid(request);
        System.out.println("setHasRead: " + talkId);
        chatService.setTalkAllMsgHasRead(uid, talkId);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/getMessageList", method = RequestMethod.GET)
    public JSONResponse getMessageList(@RequestParam Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String talkId = params.get("talkId");
        Integer index = Integer.valueOf(params.get("index"));
        Integer pageSize = 10;
        if (params.get("pageSize") != null) {
            pageSize = Integer.valueOf(params.get("pageSize"));
        }
        // 如果是第一页，要获取一次总页数，记录一下统计的起始时间
        if (index == 1) {
            Long now = System.currentTimeMillis();
            redisUtil.hset("MSG_RECORD_BEGIN_TIME", talkId, now.toString());
            Integer totalSize = chatService.getAllHistoryMessageSize(talkId, uid, new Date(now));
            Integer totalPage = 1;
            if (totalSize <= pageSize) {
                totalPage = 1;
            } else if (totalSize % pageSize == 0) {
                totalPage = totalSize / pageSize;
            } else {
                totalPage = totalSize / pageSize + 1;
            }
            List<MsgRecord> result = chatService.getMessageRecord(uid, talkId, new Date(now), index, pageSize);
            return new JSONResponse(1).putData("messageList", result).putData("allPageSize", totalPage).putData("curPageIndex", index);
        } else {
            String nowStr = redisUtil.hget("MSG_RECORD_BEGIN_TIME", talkId);
            Date beginTime = null;
            if (nowStr != null) {
                beginTime = new Date(Long.valueOf(nowStr));
            }
            List<MsgRecord> result = chatService.getMessageRecord(uid, talkId, beginTime, index, pageSize);
            return new JSONResponse(1).putData("messageList", result).putData("curPageIndex", index);
        }
    }

    @RequestMapping("/openPrivateTalk")
    public JSONResponse openPrivateTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String friendId = params.get("friendId");
        Map<String, Object> result = chatService.openNewPrivateChat(uid, friendId);
        PrivateChat privateChat = (PrivateChat) result.get("privateChat");
        return new JSONResponse(1)
                .putData("talkId", privateChat.getChatId().toString())
                .putData("isNewTalk", result.get("isNewTalk"));
    }

    @RequestMapping("/removeTalk")
    public JSONResponse removeTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String talkId = params.get("talkId");
        chatService.removePrivateChat(uid, Long.valueOf(talkId));
        return new JSONResponse(1);
    }


}
