package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.GroupNotifyFactory;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.message.NotifyPackage;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.mappers.GroupChatMapper;
import com.allen.imsystem.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupChat;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import com.allen.imsystem.model.pojo.UserChatGroup;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.IFriendQueryService;
import com.allen.imsystem.service.IGroupChatService;
import com.allen.imsystem.service.IMessageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GroupChatService implements IGroupChatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private IFriendQueryService friendService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IMessageService messageService;


    @Override
    @Transactional
    public CreateGroupDTO createNewGroupChat(String ownerId, String groupName) {
        // 如果没有输入群名，则使用默认群名
        if (StringUtils.isEmpty(groupName)) {
            groupName = GlobalConst.DEFAULT_GROUP_NAME;
        } else if (groupName.length() > 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群名称长度不得超过10个字符");
        }
        // 从gid池里取一个gid
        String gid = groupChatMapper.selectUnUsedGid();
        groupChatMapper.softDeleteUsedGid(gid);
        // 插入群聊表
        GroupChat groupChat = new GroupChat(gid, groupName, ownerId);
        groupChatMapper.insertNewGroupChat(groupChat);
        // 插入用户-群关系
        String alias = userMapper.selectUserInfoDTO(ownerId).getUsername();
        Long chatId = SnowFlakeUtil.getNextSnowFlakeId();
        UserChatGroup userChatGroup = new UserChatGroup(chatId, ownerId, gid, alias, ownerId, false);
        groupChatMapper.insertUserChatGroup(userChatGroup);
        // 缓存更新，默认会话不开启
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, ownerId + gid, true);
        return new CreateGroupDTO(gid, groupChat.getAvatar(), groupName, chatId);

    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(SendMsgDTO msg) {
        GroupMsgRecord groupMsgRecord = new GroupMsgRecord();
        groupMsgRecord.setMsgId(msg.getMsgId());
        Date createdTime = new Date(Long.parseLong(msg.getTimeStamp()));    // 以发送时间作为记录创建的时间
        groupMsgRecord.setCreatedTime(createdTime);
        groupMsgRecord.setUpdateTime(createdTime);
        groupMsgRecord.setGid(msg.getGid());
        groupMsgRecord.setStatus(true);
        groupMsgRecord.setSenderId(msg.getSrcId());
        groupMsgRecord.setMsgType(msg.getMessageType());

        switch (msg.getMessageType()) {
            case GlobalConst.MsgType.TEXT: {    // 文字消息
                groupMsgRecord.setContent(msg.getMessageText());
                break;
            }
            case GlobalConst.MsgType.IMAGE: {    // 图片消息
                groupMsgRecord.setContent("[图片]");
                String imgUrl = msg.getMessageImgUrl();
                if (StringUtils.isEmpty(imgUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(imgUrl));
                break;
            }
            case GlobalConst.MsgType.FILE: {    // 文件消息
                groupMsgRecord.setContent(msg.getFileInfo().getFileName());
                String fileUrl = msg.getFileInfo().getDownloadUrl();
                if (StringUtils.isEmpty(fileUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                groupMsgRecord.setFileMd5(fileService.getMd5FromUrl(fileUrl));
                break;
            }
        }
        // 插入新聊天记录
        groupChatMapper.insertNewGroupMsgRecord(groupMsgRecord);
        // 更新会话最后一条信息
        updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getSenderId());
    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList) {
        if (msgRecordList != null && msgRecordList.size() > 0) {
            GroupMsgRecord groupMsgRecord = msgRecordList.get(msgRecordList.size() - 1);
            updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getSenderId());
            groupChatMapper.insertNewGroupMsgRecordBatch(msgRecordList);
        }
    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord) {
        updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getSenderId());
        if (groupMsgRecord != null) {
            groupChatMapper.insertNewGroupMsgRecord(groupMsgRecord);
        }
    }


    @Override
    public List<GroupChatInfoDTO> getGroupChatList(String uid) {
        return groupChatMapper.selectGroupChatList(uid);
    }

    @Override
    public void updateGroupLastMsg(String gid, Long lastMsgId, String lastSenderId) {
        redisService.hset(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME, gid, String.valueOf(System.currentTimeMillis()));
        groupChatMapper.updateGroupLastMsg(gid, lastMsgId.toString(), lastSenderId);
    }

    @Override
    public Map<String, ChatSessionDTO> getAllGroupChatSession(String gid) {
        return groupChatMapper.selectGroupAllChatData(gid);
    }

    @Override
    @Transactional
    public boolean inviteFriendToChatGroup(String inviterId, String gid, List<InviteDTO> inviteDTOList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!checkIsGroupMember(inviterId, gid)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是该群成员，或群已解散");
        }
        List<InviteDTO> newMemberList = new ArrayList<>(inviteDTOList.size());  // 可以拉取的，保存到这个列表里
        // 判断是否可以拉成功，根据不同情况构造不同的通知内容
        GroupNotifyFactory successFactory = GroupNotifyFactory.getInstance(gid);
        GroupNotifyFactory failFactory = GroupNotifyFactory.getInstance(gid);
        for (InviteDTO dto : inviteDTOList) {
            if (friendService.checkIsDeletedByFriend(inviterId, dto.getUid())) { // 如果被对方删了
                failFactory.appendNotify(dto.getUsername() + "不是您的好友或您已被对方删除");
            } else if (checkIsGroupMember(dto.getUid(), gid)) { // 如果已经是成员
                failFactory.appendNotify(dto.getUsername() + "已经是群成员");
            } else {// 可以拉取
                dto.setChatId(SnowFlakeUtil.getNextSnowFlakeId());
                newMemberList.add(dto);
                successFactory.appendNotify(dto.getUsername() + " 加入了群聊");
            }
        }
        // 批量插入用户-群关系
        if (!newMemberList.isEmpty()) {
            // 如果曾经进过群，则重新启用原来的chat
            Map<String, UserChatGroup> oldMembers = groupChatMapper.selectUserChatGroupRelationByUidList(newMemberList, gid);
            if (!CollectionUtils.isEmpty(oldMembers)) {
                List<InviteDTO> oldMemberList = new ArrayList<>(newMemberList.size() - oldMembers.size());
                for (InviteDTO dto : newMemberList) {
                    UserChatGroup oldRelation = oldMembers.get(dto.getUid());
                    // 如果被邀请者曾经是该群成员，那么将其添加到oldMemberList
                    if (oldRelation != null) {
                        oldMemberList.add(dto);
                        // 如果被邀请者曾经是该群成员， 开启关闭状态与原来一致
                        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, dto.getUid() + gid,
                                !oldRelation.getShouldDisplay());
                    } else {// 被邀请者是新成员，会话默认是关闭状态
                        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, dto.getUid() + gid, true);
                    }
                    redisService.sSet(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid, dto.getUid());
                }
                groupChatMapper.reActivateRelation(oldMemberList, gid);  // 激活曾经进过群的会话
                newMemberList.removeAll(oldMemberList); // 去除掉所有曾经进过群的旧成员，只剩下未曾进过群的新成员
            }
            if (!CollectionUtils.isEmpty(newMemberList)) {
                UserChatGroup baseRelation = new UserChatGroup();
                baseRelation.setLastAckMsgId(null);
                baseRelation.setStatus(true);
                baseRelation.setInviterId(inviterId);
                baseRelation.setGid(gid);
                baseRelation.setShouldDisplay(false);
                groupChatMapper.insertUserChatGroupBatch(newMemberList, baseRelation);
            }
        }
        // 新启动一个线程处理通知
        new Thread(() -> {
            List<GroupMsgRecord> successNotifyList = successFactory.done();
            if (!CollectionUtils.isEmpty(successNotifyList)) {
                // 保存成功的群通知到数据库
                saveGroupChatMsgRecord(successNotifyList);
                // 推送成功的通知给所有人
                Set<Object> destIdSet = getGroupMemberFromCache(gid);
                if (destIdSet != null) {
                    NotifyPackage successNotifyPackage = NotifyPackage.builder()
                            .receivers(destIdSet)
                            .notifyContents(successNotifyList)
                            .build();
                    messageService.sendGroupNotify(destIdSet, gid, successNotifyList);
                }
            }

            List<GroupMsgRecord> failNotifyList = failFactory.done();
            if (!CollectionUtils.isEmpty(failNotifyList)) {
                // 推送失败的通知给邀请者
                if (failNotifyList.size() > 0) {
                    NotifyPackage failNotifyPackage = NotifyPackage.builder()
                            .receiver(inviterId)
                            .notifyContents(failNotifyList)
                            .build();
                    messageService.sendGroupNotify(inviterId, gid, failNotifyList);
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean checkIsGroupMember(String uid, String gid) {
        String setKey = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid;
        if (!redisService.hasKey(setKey)) {
            loadGroupMemberListIntoRedis(gid);
        }
        boolean isMember = redisService.sHasKey(setKey, uid);
        return isMember;
    }

    @Override
    public List<GroupMemberDTO> getGroupMemberList(String uid, String gid) {
        List<GroupMemberDTO> groupMemberDTOList = groupChatMapper.selectGroupMemberList(gid);
        for (GroupMemberDTO member : groupMemberDTOList) {
            Integer relation = null;
            if (uid.equals(member.getUid())) {    // 自己
                relation = 0;
            } else if (friendService.checkIsTwoWayFriend(uid, member.getUid())) {   // 是好友
                relation = 2;
            } else {  // 是非好友
                relation = 1;
            }
            member.setRelation(relation);
        }
        return groupMemberDTOList;
    }

    @Override
    public Set<Object> getGroupMemberFromCache(String gid) {
        // 如果redis中没有，从数据库中加载到redis中
        if (!redisService.hasKey(gid)) {
            loadGroupMemberListIntoRedis(gid);
        }
        return redisService.setMembers(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid);
    }

    @Override
    @Transactional
    public void leaveGroupChat(String uid, String gid) {
        //0、 群主不能退群
        String ownerId = groupChatMapper.selectGroupOwnerId(gid);
        if (uid.equals(ownerId)) {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "群主退群前请先解散群聊");
        }
        //1、 删除用户-群关系
        UserChatGroup relation = groupChatMapper.selectUserChatGroupRelation(uid, gid);
        if (relation == null) {
            return;
        }
        groupChatMapper.softDeleteGroupMember(uid, gid);
        //2、 更新缓存
        redisService.setRemove(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid, uid);
        //3、 通知群主，xxx离开了群聊
        if (StringUtils.isNotEmpty(ownerId)) {
            Set<Object> destIdSet = new HashSet<>(1);
            destIdSet.add(ownerId);
            GroupMsgRecord notify = new GroupMsgRecord(SnowFlakeUtil.getNextSnowFlakeId(), gid, gid,
                    4, relation.getUserAlias() + " 离开了群聊", "", true, new Date(), new Date());
            messageService.sendGroupNotify(destIdSet, gid, notify);
        }
    }

    @Override
    @Transactional
    public void kickOutGroupMember(List<GroupMemberDTO> memberList, String gid, String ownerId) {
        if (!checkIsGroupMember(ownerId, gid)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是群成员，或群已解散");
        }
        String realOwnerId = groupChatMapper.selectGroupOwnerId(gid);
        if (StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你不是群主");
        }
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        // 更新缓存
        Set<Object> allMember = getGroupMemberFromCache(gid);
        String key = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid;
        for (GroupMemberDTO member : memberList) {
            redisService.setRemove(key, member.getUid());
            factory.appendNotify(member.getGroupAlias() + " 被群主踢出了群聊");
        }
        // 保存群通知
        List<GroupMsgRecord> notifyList = factory.done();
        saveGroupChatMsgRecord(notifyList);
        // 发送
        messageService.sendGroupNotify(allMember, gid, notifyList);
        // 更新数据库
        groupChatMapper.softDeleteGroupMemberBatch(memberList, gid);
    }

    @Override
    @Transactional
    public void dismissGroupChat(String ownerId, String gid) {
        String realOwnerId = groupChatMapper.selectGroupOwnerId(gid);
        if (StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你不是群主");
        }
        Set<Object> allMember = getGroupMemberFromCache(gid);
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        factory.appendNotify("该群已被群主解散");
        List<GroupMsgRecord> notify = factory.done();
        saveGroupChatMsgRecord(factory.done());
        // 发送群通知
        messageService.sendGroupNotify(allMember, gid, notify);

        // 数据库
        groupChatMapper.softDeleteAllMember(gid);
        groupChatMapper.softDeleteGroupChat(gid);

        // 缓存删除
        redisService.del(GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid);
    }

    @Override
    public void changeUserGroupAlias(String uid, String gid, String alias) {
        if (StringUtils.isEmpty(alias) || alias.length() >= 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群昵称长度不合法");
        }
        UserChatGroup relation = groupChatMapper.selectUserChatGroupRelation(uid, gid);
        if (relation == null) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是群成员");
        }
        relation.setUserAlias(alias);
        groupChatMapper.updateUserGroupChat(relation);
    }

    @Override
    public Map<String, String> updateGroupInfo(MultipartFile multipartFile, String groupName, String gid, String uid) {
        Map<String, String> result = new HashMap<>(3);
        GroupChat groupChat = new GroupChat();
        groupChat.setGid(gid);
        if (multipartFile != null) {
            String url = fileService.uploadAvatar(multipartFile, gid);
            result.put("groupAvatar", GlobalConst.Path.AVATAR_URL + url);
            groupChat.setAvatar(url);
        }
        if (StringUtils.isNotEmpty(groupName) && groupName.length() <= 10) {
            groupChat.setGroupName(groupName);
            result.put("groupName", groupName);
        } else {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群名长度应在1-10个字符之间");
        }
        groupChatMapper.updateGroupChat(groupChat);
        return result;
    }

    @Override
    public String getGidFromChatId(Long chatId) {
        String key = GlobalConst.Redis.KEY_CHAT_GID_MAP + chatId.toString();
        if (!redisService.hasKey(key)) {
            String gid = groupChatMapper.selectGidFromChatId(chatId);
            if (gid != null) {
                redisService.set(key, gid, 10L, TimeUnit.MINUTES);
            } else {
                throw new BusinessException(ExceptionType.SERVER_ERROR);
            }
        }
        return (String) redisService.get(key);
    }

    private void loadGroupMemberListIntoRedis(String gid) {
        Set<String> groupMemberIdSet = groupChatMapper.selectGroupMemberIdSet(gid);
        if (groupMemberIdSet != null && groupMemberIdSet.size() > 0) {
            String setKey = GlobalConst.Redis.KET_GROUP_CHAT_MEMBERS + gid;
            redisService.del(setKey);
            redisService.sSetAndTime(setKey, 2 * 60 * 60L, groupMemberIdSet.toArray());
        }
    }


}
