package com.allen.imsystem.chat.controller;

import com.alibaba.fastjson.JSONArray;
import com.allen.imsystem.chat.model.param.CreateGroupParam;
import com.allen.imsystem.chat.model.param.KickMemberParam;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.bean.JSONResponse;
import com.allen.imsystem.common.cache.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.friend.model.param.InviteFriendToGroupParam;
import com.allen.imsystem.friend.model.param.InviteParam;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.vo.GroupView;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api("聊天与会话相关接口")
@RequestMapping("/api/talk")
@RestController
public class TalkController {

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @Autowired
    private ChatService chatService;

    @Autowired
    private GroupChatService groupChatService;

    /**
     * 获取会话的一些信息，只在用户点击一个会话的时候，调用此接口，故可认为对于该用户该会话所有消息已读。
     */
    @ApiOperation("获取会话的一些信息")
    @RequestMapping(value = "/getTalkData", method = RequestMethod.GET)
    public JSONResponse getTalkData(@RequestParam("talkId") String chatIdStr, HttpServletRequest request) {
        Long chatId = Long.valueOf(chatIdStr);
        String uid = cacheHolder.getUid(request);
        ChatSessionInfo chatSessionInfo = chatService.getChatInfo(chatId, uid);
        if(chatSessionInfo == null){
            throw new BusinessException(ExceptionType.TALK_NOT_VALID);
        }
        if(chatSessionInfo.getIsGroup()){
            chatService.setGroupChatAllMsgHasRead(uid,chatSessionInfo.getGid());
        }else{
            chatService.setPrivateChatAllMsgHasRead(uid, chatId);
        }
        return new JSONResponse(1).putAllData(new BeanMap(chatSessionInfo));
    }

    /**
     * 获取用户的会话列表（相当于一打开微信看到的那个列表）
     */
    @ApiOperation("获取用户的会话列表")
    @RequestMapping(value = "/getTalkList", method = RequestMethod.GET)
    public JSONResponse getTalkList(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        List<ChatSession> chatSessionList = chatService.getChatList(uid);
        return new JSONResponse(1).putData("talkList", chatSessionList);
    }

    /**
     * 将一个会话所有信息设为已读
     */
    @RequestMapping(value = "/setHasRead", method = RequestMethod.POST)
    public JSONResponse setHasRead(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long chatId = Long.parseLong(params.get("talkId"));
        String uid = cacheHolder.getUid(request);
        if(GlobalConst.ChatType.PRIVATE_CHAT.equals(chatService.getChatType(chatId))){
            chatService.setPrivateChatAllMsgHasRead(uid, chatId);
        }else{
            String gid = params.get("gid");
            chatService.setGroupChatAllMsgHasRead(uid,gid);
        }
        return new JSONResponse(1);
    }

    /**
     * 获取一个会话的聊天记录（分页）
     */
    @RequestMapping(value = "/getMessageList", method = RequestMethod.GET)
    public JSONResponse getMessageList(@RequestParam Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        Long chatId = Long.valueOf(params.get("talkId"));
        Integer index = Integer.valueOf(params.get("index"));
        Integer pageSize = 10;
        if (params.get("pageSize") != null) {
            pageSize = Integer.valueOf(params.get("pageSize"));
        }
        boolean isGroup = GlobalConst.ChatType.GROUP_CHAT.equals(chatService.getChatType(chatId));
        Map<String, Object> resultMap = chatService.getMessageRecord(isGroup, uid, chatId, index, pageSize);
        return new JSONResponse(1).putAllData(resultMap);
    }

    /**
     * 开启一个私聊会话（主动创建一个与好友的会话，并添加到会话列表)
     */
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

    /**
     * 开启一个群聊会话（主动创建一个与好友的会话，并添加到会话列表)
     */
    @RequestMapping("/openGroupTalk")
    public JSONResponse openGroupTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        Map<String, Object> result = groupChatService.openGroupChat(uid, gid);
        GroupChat relation = (GroupChat) result.get("relation");
        return new JSONResponse(1)
                .putData("talkId", relation.getChatId().toString())
                .putData("isNewTalk", result.get("isNewTalk"));
    }

    /**
     * 从会话列表移除一个会话(群会话、私聊会话)
     */
    @RequestMapping("/removeTalk")
    public JSONResponse removeTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        Long chatId = Long.valueOf(params.get("talkId"));
        Integer chatType = chatService.getChatType(chatId);
        if (GlobalConst.ChatType.PRIVATE_CHAT.equals(chatType)) {
            chatService.removePrivateChat(uid, chatId);
        } else if(GlobalConst.ChatType.GROUP_CHAT.equals(chatType)){
            chatService.removeGroupChat(uid, chatId);
        }
        return new JSONResponse(1);
    }

    /**
     * 创建群聊
     */
    @RequestMapping(value = "/createGroupTalk", method = RequestMethod.POST)
    public JSONResponse createGroupTalk(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String groupName = params.get("groupName");
        GroupView groupView = groupChatService.createGroup(uid, groupName);
        return new JSONResponse(1).putAllData(new BeanMap(groupView));
    }

    /**
     * 用户获取群聊列表
     */
    @RequestMapping(value = "/getGroupTalkList", method = RequestMethod.GET)
    public JSONResponse getGroupTalkList(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        List<GroupView> resultList = groupChatService.findGroupList(uid);
        return new JSONResponse(1).putData("groupTalkList", resultList);
    }

    /**
     * 用户获取某个群的群成员列表
     */
    @RequestMapping(value = "/getGroupTalkMember", method = RequestMethod.GET)
    public JSONResponse getGroupTalkMember(@RequestParam("gid") String gid, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        List<GroupMemberView> groupMemberList = groupChatService.getGroupMemberList(uid, gid);
        return new JSONResponse(1).putData("memberList", groupMemberList);
    }

    /**
     * 用户拉取好友入群（批量）
     */
    @RequestMapping(value = "/inviteFriendToGroupTalk", method = RequestMethod.POST)
    public JSONResponse inviteFriendToGroupTalk(@RequestBody InviteFriendToGroupParam param, HttpServletRequest request) throws Exception {
        String gid = param.getGid();
        List<InviteParam> invitedFriendList = param.getInviteFriendList();
        String inviterId = cacheHolder.getUid(request);
        boolean success = groupChatService.inviteFriendToChatGroup(inviterId, gid, invitedFriendList);
        return new JSONResponse(1);
    }

    /**
     * 用户退群
     */
    @RequestMapping(value = "/leaveGroupTalk", method = RequestMethod.POST)
    public JSONResponse leaveGroupTalk(@RequestBody Map<String, String> params, HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        groupChatService.leaveGroupChat(uid, gid);
        return new JSONResponse(1);
    }

    /**
     * 群主解散群聊
     */
    @RequestMapping(value = "/dismissGroupTalk", method = RequestMethod.POST)
    public JSONResponse dismissGroupTalk(@RequestBody Map<String, String> params, HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = params.get("gid");
        groupChatService.dismissGroupChat(uid, gid);
        return new JSONResponse(1);
    }

    /**
     * 群主踢人（批量）
     */
    @RequestMapping(value = "/removeMemberFromGroupTalk", method = RequestMethod.POST)
    public JSONResponse removeMemberFromGroupTalk(@RequestBody KickMemberParam param, HttpServletRequest request) throws Exception {
        String uid = cacheHolder.getUid(request);
        String gid = param.getGid();
        List<GroupMemberView> memberList = param.getMemberList();
        groupChatService.kickOutGroupMember(memberList, gid, uid);
        return new JSONResponse(1);
    }

    /**
     * 用户更改自己的群昵称
     */
    @RequestMapping(value = "/changeGroupAlias", method = RequestMethod.POST)
    public JSONResponse changeGroupAlias(@RequestBody Map<String, String> params, HttpServletRequest request) throws Exception {
        String gid = params.get("gid");
        String groupAlias = params.get("groupAlias");
        String uid = cacheHolder.getUid(request);
        groupChatService.changeUserGroupAlias(uid, gid, groupAlias);
        return new JSONResponse(1);
    }

    /**
     * 更新群信息（群头像、群名）
     */
    @RequestMapping(value = "/updateGroupTalkInfo", method = RequestMethod.POST)
    public JSONResponse updateGroupTalkInfo(@RequestParam("gid") String gid,
                                          @RequestParam(value = "groupName", required = false) String groupName,
                                          @RequestParam(value = "groupAvatar", required = false) MultipartFile multipartFile,
                                          HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        Map<String, String> result = groupChatService.updateGroupInfo(multipartFile, groupName, gid, uid);
        return new JSONResponse(1).putAllData(result);
    }

    /**
     * 检验某个chatId是否有效
     */
    @RequestMapping(value = "/validateTalkId",method = RequestMethod.GET)
    public JSONResponse validateTalkId(@RequestParam("talkId")String talkId, HttpServletRequest request){
        if(StringUtils.isEmpty(talkId) || !StringUtils.isNumeric(talkId)){
            return new JSONResponse().success().putData("hasThisTalk",false);
        }
        String uid = cacheHolder.getUid(request);
        Long chatId = Long.parseLong(talkId);
        Map<String,Object> result = chatService.validateChatId(chatId,uid);
        return new JSONResponse().success().putAllData(result);
    }

}
