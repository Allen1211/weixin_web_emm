package com.allen.imsystem.controller;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.MultipartFileUtil;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.impl.RedisService;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RequestMapping("/api/talk")
@RestController
public class TalkController {

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @Autowired
    private IChatService chatService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IFileService fileService;

    /**
     * 获取会话的一些信息，只在用户点击一个会话的时候，调用此接口，故可认为对于该用户该会话所有消息已读。
     * @param talkIdStr
     * @param request
     * @return
     */
    @RequestMapping(value = "/getTalkData", method = RequestMethod.GET)
    public JSONResponse getTalkData(@RequestParam("talkId") String talkIdStr, HttpServletRequest request) {
        Long talkId = Long.valueOf(talkIdStr);
        String uid = cacheHolder.getUid(request);
        ChatSessionInfo chatSessionInfo = chatService.getChatInfo(talkId, uid);
        chatService.setTalkAllMsgHasRead(uid,talkIdStr);
        Long talkLastMsgTimestamp = chatService.getChatLastMsgTimestamp(Long.parseLong(talkIdStr));
        return new JSONResponse(1)
                .putData("isGroup", chatSessionInfo.getIsGroup())
                .putData("title", chatSessionInfo.getTitle())
                .putData("isGroupOwner", chatSessionInfo.getIsGroupOwner())
                .putData("talkId", chatSessionInfo.getTalkId().toString())
                .putData("srcId",chatSessionInfo.getSrcId())
                .putData("destId",chatSessionInfo.getDestId())
                .putData("avatar",chatSessionInfo.getAvatar())
                .putData("lastTimeStamp",talkLastMsgTimestamp);
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
            redisService.hset("MSG_RECORD_BEGIN_TIME", talkId, now.toString());
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
            String nowStr = (String) redisService.hget(GlobalConst.Redis.KEY_RECORD_BEGIN_TIME, talkId);
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


    @RequestMapping(value = "/uploadMessageImage",method = RequestMethod.POST)
    public JSONResponse uploadMessageImage(@RequestParam("image") MultipartFile multipartFile){
       try {
           String url = fileService.uploadMsgImg(multipartFile);
           return new JSONResponse(1).putData("imageUrl",url);
       }catch(IOException e){
           e.printStackTrace();
           throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE, "文件保存失败");
       }
    }

    @RequestMapping(value = "/uploadMultipartFile",method = RequestMethod.POST)
    public JSONResponse uploadMultipartFile(HttpServletRequest request) throws Exception {
        //使用 工具类解析相关参数，工具类代码见下面
        MultipartFileDTO param = MultipartFileUtil.parse(request);

        MultiFileResponse responseDTO = fileService.uploadMultipartFile(param);
        return new JSONResponse(1).putAllData(new BeanMap(responseDTO));
    }

    @RequestMapping(value = "/getFileUploadInfo",method = RequestMethod.GET)
    public JSONResponse getFileUploadInfo(@RequestParam("md5")String md5){
        FileUploadInfo fileUploadInfo = fileService.getUnCompleteParts(md5);
        return new JSONResponse(1).putAllData(new BeanMap(fileUploadInfo));
    }
}
