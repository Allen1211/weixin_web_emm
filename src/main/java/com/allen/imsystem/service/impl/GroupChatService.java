package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.GroupNotifyFactory;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.GroupChatMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupChat;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import com.allen.imsystem.model.pojo.UserChatGroup;
import com.allen.imsystem.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class GroupChatService implements IGroupChatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IMessageService messageService;


    @Override
    @Transactional
    public  CreateGroupDTO createNewGroupChat(String ownerId, String groupName) {
        if(StringUtils.isEmpty(groupName)){
            groupName = GlobalConst.DEFAULT_GROUP_NAME;
        }

        String gid = groupChatMapper.selectUnUsedGid();
        groupChatMapper.softDeleteUsedGid(gid);
        GroupChat groupChat = new GroupChat(gid,groupName,ownerId);
        groupChatMapper.insertNewGroupChat(groupChat);

        String alias = userMapper.selectSenderInfo(ownerId).getUsername();
        Long chatId = SnowFlakeUtil.getNextSnowFlakeId();
        UserChatGroup userChatGroup = new UserChatGroup(chatId,ownerId,gid,alias,ownerId,true);
        groupChatMapper.insertUserChatGroup(userChatGroup);
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,ownerId+chatId,false);
        redisService.hset(GlobalConst.Redis.KEY_CHAT_TYPE,chatId.toString(),GlobalConst.ChatType.GROUP_CHAT);
        CreateGroupDTO result = new CreateGroupDTO(gid,groupChat.getAvatar(),groupName,chatId);
        return result;

    }
    @Override
    @Transactional
    public void saveGroupChatMsgRecord(SendMsgDTO msg){
        GroupMsgRecord groupMsgRecord = new GroupMsgRecord();
        groupMsgRecord.setMsgId(msg.getMsgId());
//        Date createdTime = new Date(Long.parseLong(msg.getTimeStamp()));
        Date createdTime = new Date();
        groupMsgRecord.setCreatedTime(createdTime);
        groupMsgRecord.setUpdateTime(createdTime);
        groupMsgRecord.setGid(msg.getGid());
        groupMsgRecord.setStatus(true);
        groupMsgRecord.setSenderId(msg.getSrcId());
        groupMsgRecord.setMsgType(msg.getMessageType());
        switch (msg.getMessageType()){
            case 1:{    // 文字消息
                groupMsgRecord.setContent(msg.getMessageText());
                break;
            }
            case 2:{    // 图片消息
                groupMsgRecord.setContent("[图片]");
                String imgUrl = msg.getMessageImgUrl();
                if(StringUtils.isEmpty(imgUrl)) throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(2,imgUrl));
                break;
            }
            case 3:{    // 文件消息
                groupMsgRecord.setContent(msg.getFileInfo().getFileName());
                String fileUrl = msg.getFileInfo().getDownloadUrl();
                if(StringUtils.isEmpty(fileUrl)) throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(3,fileUrl));
                break;
            }
        }
        groupChatMapper.insertNewGroupMsgRecord(groupMsgRecord);
        updateGroupLastMsg(groupMsgRecord.getGid(),groupMsgRecord.getMsgId(),groupMsgRecord.getSenderId());
    }
    @Override
    @Transactional
    public void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList){
        if(msgRecordList!=null && msgRecordList.size()>0){
            GroupMsgRecord groupMsgRecord = msgRecordList.get(msgRecordList.size()-1);
            updateGroupLastMsg(groupMsgRecord.getGid(),groupMsgRecord.getMsgId(),groupMsgRecord.getSenderId());
            groupChatMapper.insertNewGroupMsgRecordBatch(msgRecordList);
        }
    }
    @Override
    @Transactional
    public void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord){
        updateGroupLastMsg(groupMsgRecord.getGid(),groupMsgRecord.getMsgId(),groupMsgRecord.getSenderId());
        if(groupMsgRecord!=null){
            groupChatMapper.insertNewGroupMsgRecord(groupMsgRecord);
        }
    }



    @Override
    public List<GroupChatInfoDTO> getGroupChatList(String uid) {
        return groupChatMapper.selectGroupChatList(uid);
    }

    @Override
    public void updateGroupLastMsg(String gid, Long lastMsgId,String lastSenderId){
        redisService.hset(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME,gid,String.valueOf(System.currentTimeMillis()));
        groupChatMapper.updateGroupLastMsg(gid,lastMsgId.toString(),lastSenderId);
    }

    @Override
    public Map<String, ChatSessionDTO> getAllGroupChatSession(String gid) {
        return groupChatMapper.selectGroupAllChatData(gid);
    }

    @Override
    @Transactional
    public boolean inviteFriendToChatGroup(String inviterId,String gid, List<InviteDTO> inviteDTOList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        UserChatGroup userChatGroup = new UserChatGroup();
        userChatGroup.setLastAckMsgId(null);
        userChatGroup.setStatus(true);
        userChatGroup.setInviterId(inviterId);
        userChatGroup.setGid(gid);
        userChatGroup.setShouldDisplay(true);
        List<InviteDTO> validList = new ArrayList<>(inviteDTOList.size());
        // 判断是否可以拉成功，根据不同情况构造不同的通知内容
        GroupNotifyFactory successFactory = GroupNotifyFactory.getInstance(gid);
        GroupNotifyFactory failFactory = GroupNotifyFactory.getInstance(gid);
        for(InviteDTO dto : inviteDTOList){
            if(friendService.checkIsDeletedByFriend(inviterId,dto.getUid())){            // 如果被删了
                failFactory.appendNotify(dto.getUsername() + "不是您的好友或您已被对方删除");
            }else if(checkIsGroupMember(dto.getUid(),gid)){ // 如果已经是成员
                failFactory.appendNotify(dto.getUsername() + "已经是群成员");
            }else{
                Long chatId = SnowFlakeUtil.getNextSnowFlakeId();
                dto.setChatId(chatId);
                redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,dto.getUid()+chatId,true);
                redisService.sSet(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid,dto.getUid());
                validList.add(dto);
                successFactory.appendNotify(dto.getUsername()+" 加入了群聊");
            }
        }
        if(!validList.isEmpty()){
            groupChatMapper.insertUserChatGroupBatch(validList,userChatGroup);
        }
        List<GroupMsgRecord> successNotifyList = successFactory.done();
        if(successNotifyList.size()>0){
            // 保存成功的群通知到数据库
            saveGroupChatMsgRecord(successNotifyList);
            // 推送成功的通知给所有人
            Set<Object> destIdSet = getGroupMemberFromCache(gid);
            if(destIdSet!=null){
                messageService.sendGroupNotify(destIdSet,gid,successNotifyList);
            }
        }
        // 推送失败的通知给邀请者
        List<GroupMsgRecord> failNotifyList = failFactory.done();
        if(failNotifyList.size() > 0){
            messageService.sendGroupNotify(inviterId,gid,failNotifyList);
        }
        return true;
    }

    @Override
    public boolean checkIsGroupMember(String uid, String gid) {
        String setKey = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid;
        if(! redisService.hasKey(setKey)){
            loadGroupMemberListIntoRedis(gid);
        }
        boolean isMember = redisService.sHasKey(setKey,uid);
        return isMember;
    }

    @Override
    public List<GroupMemberDTO> getGroupMemberList(String uid,String gid) {
        List<GroupMemberDTO> groupMemberDTOList = groupChatMapper.selectGroupMemberList(gid);
        for(GroupMemberDTO member : groupMemberDTOList){
            Integer relation = null;
            if(uid.equals(member.getUid())){
                relation = 0;
            }else if(friendService.checkIsTwoWayFriend(uid,member.getUid())){
                relation = 2;
            }else{
                relation = 1;
            }
            member.setRelation(relation);
        }
        return groupMemberDTOList;
    }

    @Override
    public Set<Object> getGroupMemberFromCache(String gid) {
        if(!redisService.hasKey(gid)){
            loadGroupMemberListIntoRedis(gid);
        }
        return redisService.setMembers(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid);
    }

    @Override
    @Transactional
    public void leaveGroupChat(String uid, String gid) {
        //0、 群主不能退群
        String ownerId = groupChatMapper.selectGroupOwnerId(gid);
        if(uid.equals(ownerId)){
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL,"群主退群前请先解散群聊");
        }
        //1、 删除用户-群关系
        UserChatGroup relation = groupChatMapper.selectUserChatGroupRelation(uid,gid);
        if(relation == null){
            return;
        }
        groupChatMapper.softDeleteGroupMember(uid,gid);
        //2、 更新缓存
        redisService.setRemove(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid,uid);
        //3、 通知群主，xxx离开了群聊
        if(StringUtils.isNotEmpty(ownerId)){
            Set<Object> destIdSet = new HashSet<>(1);
            destIdSet.add(ownerId);
            GroupMsgRecord notify = new GroupMsgRecord(SnowFlakeUtil.getNextSnowFlakeId(),gid,gid,
                    4,relation.getUserAlias()+" 离开了群聊","",true,new Date(),new Date());
            messageService.sendGroupNotify(destIdSet,gid,notify);
        }
    }

    @Override
    @Transactional
    public void kickOutGroupMember(List<GroupMemberDTO> memberList, String gid, String ownerId) {
        if(! checkIsGroupMember(ownerId,gid)){
            throw new BusinessException(ExceptionType.PERMISSION_DENIED,"你还不是群成员");
        }
        String realOwnerId = groupChatMapper.selectGroupOwnerId(gid);
        if(StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)){
            throw new BusinessException(ExceptionType.PERMISSION_DENIED,"你不是群主");
        }
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        // 更新缓存
        Set<Object> allMember = getGroupMemberFromCache(gid);
        String key = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid;
        for(GroupMemberDTO member: memberList){
            redisService.setRemove(key,member.getUid());
            factory.appendNotify(member.getGroupAlias() + " 被群主踢出了群聊");
        }
        // 保存群通知
        List<GroupMsgRecord> notifyList = factory.done();
        saveGroupChatMsgRecord(notifyList);
        // 发送
        messageService.sendGroupNotify(allMember,gid,notifyList);
        // 更新数据库
        groupChatMapper.softDeleteGroupMemberBatch(memberList,gid);
    }

    @Override
    @Transactional
    public void dismissGroupChat(String ownerId, String gid) {
        String realOwnerId = groupChatMapper.selectGroupOwnerId(gid);
        if(StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)){
            throw new BusinessException(ExceptionType.PERMISSION_DENIED,"你不是群主");
        }
        Set<Object> allMember = getGroupMemberFromCache(gid);
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        factory.appendNotify("该群已被群主解散");
        List<GroupMsgRecord> notify = factory.done();
        saveGroupChatMsgRecord(factory.done());
        // 发送群通知
        messageService.sendGroupNotify(allMember,gid,notify);

        // 数据库
        groupChatMapper.softDeleteAllMember(gid);
        groupChatMapper.softDeleteGroupChat(gid);

        // 缓存删除
        redisService.del(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS);
    }

    @Override
    public void changeUserGroupAlias(String uid,String gid, String alias) {
        if(StringUtils.isEmpty(alias) || alias.length() >= 10){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"群昵称长度不合法");
        }
        UserChatGroup relation = groupChatMapper.selectUserChatGroupRelation(uid,gid);
        if(relation == null){
            throw new BusinessException(ExceptionType.PERMISSION_DENIED,"你还不是群成员");
        }
        relation.setUserAlias(alias);
        groupChatMapper.updateUserGroupChat(relation);
    }
    @Override
    public Map<String,String> updateGroupInfo(MultipartFile multipartFile,String groupName,String gid,String uid){
        Map<String,String> result = new HashMap<>(3);
        GroupChat groupChat = new GroupChat();
        groupChat.setGid(gid);
        if(multipartFile != null){
            String url = fileService.uploadAvatar(multipartFile,gid);
            result.put("groupAvatar",GlobalConst.Path.AVATAR_URL+url);
            groupChat.setAvatar(url);
        }
        if(StringUtils.isNotEmpty(groupName) && groupName.length() < 10){
            groupChat.setGroupName(groupName);
            result.put("groupName",groupName);
        }
        groupChatMapper.updateGroupChat(groupChat);
        return result;
    }

    private void loadGroupMemberListIntoRedis(String gid){
        Set<String> groupMemberIdSet = groupChatMapper.selectGroupMemberIdSet(gid);
        if(groupMemberIdSet != null && groupMemberIdSet.size()>0){
            String setKey = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS+gid;
            redisService.del(setKey);
            redisService.sSetAndTime(setKey,2*60*60L,groupMemberIdSet.toArray());
        }
    }


}
