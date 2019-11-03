package com.allen.imsystem.controller;

import com.alibaba.fastjson.JSONArray;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.MultipartFileUtil;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupChat;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.UserChatGroup;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.IGroupChatService;
import com.allen.imsystem.service.impl.RedisService;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    private RedisService redisService;

    @Autowired
    private IGroupChatService groupChatService;

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
        return new JSONResponse(1).putAllData(new BeanMap(chatSessionInfo));
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
        boolean isGroup = GlobalConst.ChatType.GROUP_CHAT.equals(chatService.getChatType(Long.valueOf(talkId)));
        Map<String,Object> resultMap = chatService.getMessageRecord(isGroup,uid,talkId,new Date(),index,pageSize);
        return new JSONResponse(1).putAllData(resultMap);
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

    @RequestMapping("/openGroupTalk")
    public JSONResponse openGroupTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        Map<String, Object> result = chatService.openGroupChat(uid, gid);
        UserChatGroup relation = (UserChatGroup) result.get("relation");
        return new JSONResponse(1)
                .putData("talkId", relation.getChatId().toString())
                .putData("isNewTalk", result.get("isNewTalk"));
    }

    @RequestMapping("/removeTalk")
    public JSONResponse removeTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        Long chatId = Long.valueOf(params.get("talkId"));
        Integer chatType = chatService.getChatType(chatId);
        if(GlobalConst.ChatType.PRIVATE_CHAT.equals(chatType)){
            chatService.removePrivateChat(uid, chatId);
        }else{
            chatService.removeGroupChat(uid,chatId);
        }
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

    @RequestMapping(value = "/createGroupTalk",method = RequestMethod.POST)
    public JSONResponse createGroupTalk(@RequestBody Map<String ,String> params,HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        String groupName = params.get("groupName");
        CreateGroupDTO groupChat = groupChatService.createNewGroupChat(uid,groupName);
        groupChat.setGroupAvatar(GlobalConst.Path.RESOURCES_URL+groupChat.getGroupAvatar());
        return new JSONResponse(1).putAllData(new BeanMap(groupChat));
    }

    @RequestMapping(value = "/getGroupTalkList",method = RequestMethod.GET)
    public JSONResponse getGroupTalkList(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        List<GroupChatInfoDTO> resultList = groupChatService.getGroupChatList(uid);
        return new JSONResponse(1).putData("groupTalkList",resultList);
    }

    @RequestMapping(value = "/getGroupTalkMember",method = RequestMethod.GET)
    public JSONResponse getGroupTalkMember(@RequestParam("gid")String gid,HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        List<GroupMemberDTO> groupMemberList = groupChatService.getGroupMemberList(uid,gid);
        return new JSONResponse(1).putData("memberList",groupMemberList);
    }


    @RequestMapping(value = "/inviteFriendToGroupTalk",method = RequestMethod.POST)
    public JSONResponse inviteFriendToGroupTalk(@RequestBody Map<String,String> params,HttpServletRequest request) throws Exception {
        String gid = params.get("gid");
        String list =  params.get("friendList");
        List<InviteDTO> inviteDTOList = JSONArray.parseArray(list,InviteDTO.class);
        String inviterId = cacheHolder.getUid(request);
        boolean success = groupChatService.inviteFriendToChatGroup(inviterId,gid,inviteDTOList);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/leaveGroupTalk",method = RequestMethod.POST)
    public JSONResponse leaveGroupTalk(@RequestBody Map<String,String> params,HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        groupChatService.leaveGroupChat(uid,gid);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/dismissGroupTalk",method = RequestMethod.POST)
    public JSONResponse dismissGroupTalk(@RequestBody Map<String,String> params,HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        groupChatService.dismissGroupChat(uid,gid);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/removeMemberFromGroupTalk",method = RequestMethod.POST)
    public JSONResponse removeMemberFromGroupTalk(@RequestBody Map<String,String> params,HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        String list =  params.get("memberList");
        List<GroupMemberDTO> memberList = JSONArray.parseArray(list,GroupMemberDTO.class);
        groupChatService.kickOutGroupMember(memberList,gid,uid);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/changeGroupAlias",method = RequestMethod.POST)
    public JSONResponse changeGroupAlias(@RequestBody Map<String,String> params,HttpServletRequest request) throws Exception {
        String gid = params.get("gid");
        String groupAlias =  params.get("groupAlias");
        String uid = cacheHolder.getUid(request);
        groupChatService.changeUserGroupAlias(uid,gid,groupAlias);
        return new JSONResponse(1);
    }




}
