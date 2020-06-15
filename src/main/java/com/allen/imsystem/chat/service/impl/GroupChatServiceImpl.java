package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.model.param.CreateGroupParam;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.service.impl.GroupNotifyFactory;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.message.service.impl.NotifyPackage;
import com.allen.imsystem.chat.mappers.group.GroupChatMapper;
import com.allen.imsystem.chat.mappers.group.GroupMapper;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.message.mappers.GroupMsgRecordMapper;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.friend.model.param.InviteParam;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.vo.GroupView;

import static com.allen.imsystem.common.Const.GlobalConst.*;

import com.allen.imsystem.common.redis.RedisService;
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
public class GroupChatServiceImpl implements GroupChatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private GroupMsgRecordMapper groupMsgRecordMapper;

    @Autowired
    private FriendQueryService friendService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IdPoolService idPoolService;

    @Override
    @Transactional
    public GroupView createGroup(String ownerId, String groupName) {
        // 如果没有输入群名，则使用默认群名
        if (StringUtils.isEmpty(groupName)) {
            groupName = GlobalConst.DEFAULT_GROUP_NAME;
        } else if (groupName.length() > 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群名称长度不得超过10个字符");
        }
        // 从gid池里取一个gid
        String gid = idPoolService.nextId(IdType.GID);
        // 插入群聊表
        Group group = new Group(gid, groupName, ownerId);
        groupMapper.insert(group);
        // 插入用户-群关系
        String alias = userMapper.selectUserInfoDTO(ownerId).getUsername();
        Long chatId = idPoolService.nextChatId(ChatType.GROUP_CHAT);
        GroupChat groupChat = new GroupChat(chatId, ownerId, gid, alias, ownerId, false);
        groupChatMapper.insert(groupChat);
        // 缓存更新，默认会话不开启
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, ownerId + gid, true);
        return new GroupView(group);

    }

    @Override
    @Transactional
    public GroupMsgRecord saveGroupChatMsgRecord(SendMsgDTO msg) {
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
            case MsgType.GROUP_NOTIFY:
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
        groupMsgRecordMapper.insertNewGroupMsgRecord(groupMsgRecord);
        return groupMsgRecord;
    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList) {
        if (msgRecordList != null && msgRecordList.size() > 0) {
            GroupMsgRecord groupMsgRecord = msgRecordList.get(msgRecordList.size() - 1);
            updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getContent(),
                    groupMsgRecord.getCreatedTime(), groupMsgRecord.getSenderId());
            groupMsgRecordMapper.insertNewGroupMsgRecordBatch(msgRecordList);
        }
    }

    @Override
    @Transactional
    public void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord) {
        updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getContent(),
                groupMsgRecord.getCreatedTime(), groupMsgRecord.getSenderId());
        groupMsgRecordMapper.insertNewGroupMsgRecord(groupMsgRecord);
    }


    @Override
    public List<GroupView> findGroupList(String uid) {
        return groupChatMapper.findGroupViewListByUid(uid);
    }

    @Override
    public void updateGroupLastMsg(String gid, Long lastMsgId, String lastMsgContent, Date lastMsgCreateTime,
                                   String lastSenderId) {
        redisService.hset(RedisKey.KEY_CHAT_LAST_MSG_TIME, gid, String.valueOf(System.currentTimeMillis()));
        groupMapper.updateGroupLastMsg(gid, lastMsgId, lastMsgContent, lastMsgCreateTime, lastSenderId);
    }

    @Override
    public Map<String, ChatSession> getAllGroupChatSession(String gid) {
        return groupChatMapper.selectGroupAllChatData(gid);
    }

    @Override
    @Transactional
    public boolean inviteFriendToChatGroup(String inviterId, String gid, List<InviteParam> inviteParamList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (!checkIsGroupMember(inviterId, gid)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是该群成员，或群已解散");
        }
        List<InviteParam> newMemberList = new ArrayList<>(inviteParamList.size());  // 可以拉取的，保存到这个列表里
        // 判断是否可以拉成功，根据不同情况构造不同的通知内容
        GroupNotifyFactory successFactory = GroupNotifyFactory.getInstance(gid);
        GroupNotifyFactory failFactory = GroupNotifyFactory.getInstance(gid);
        for (InviteParam dto : inviteParamList) {
            if (friendService.checkIsDeletedByFriend(inviterId, dto.getUid())) { // 如果被对方删了
                failFactory.appendNotify(dto.getUsername() + "不是您的好友或您已被对方删除");
            } else if (checkIsGroupMember(dto.getUid(), gid)) { // 如果已经是成员
                failFactory.appendNotify(dto.getUsername() + "已经是群成员");
            } else {// 可以拉取
                newMemberList.add(dto);
                successFactory.appendNotify(dto.getUsername() + " 加入了群聊");
            }
        }
        // 批量插入用户-群关系
        if (!newMemberList.isEmpty()) {
            // 如果曾经进过群，则重新启用原来的chat
            Map<String, GroupChat> oldMembers = groupChatMapper.selectUserChatGroupRelationByUidList(newMemberList, gid);
            if (!CollectionUtils.isEmpty(oldMembers)) {
                List<InviteParam> oldMemberList = new ArrayList<>(newMemberList.size() - oldMembers.size());
                for (InviteParam newMember : newMemberList) {
                    GroupChat oldRelation = oldMembers.get(newMember.getUid());
                    // 如果被邀请者曾经是该群成员，那么将其添加到oldMemberList
                    if (oldRelation != null) {
                        oldMemberList.add(newMember);
                        // 如果被邀请者曾经是该群成员， 开启关闭状态与原来一致
                        redisService.hset(RedisKey.KEY_CHAT_REMOVE, newMember.getUid() + gid,
                                !oldRelation.getShouldDisplay());
                    } else {// 被邀请者是新成员，会话默认是关闭状态
                        redisService.hset(RedisKey.KEY_CHAT_REMOVE, newMember.getUid() + gid, true);
                    }
                    redisService.sSet(RedisKey.KET_GROUP_CHAT_MEMBERS + gid, newMember.getUid());
                }
                groupChatMapper.reActivateRelation(oldMemberList, gid);  // 激活曾经进过群的会话
                newMemberList.removeAll(oldMemberList); // 去除掉所有曾经进过群的旧成员，只剩下未曾进过群的新成员
            }
            // 批量插入所选好友的用户-群关系，新建他们的群会话
            if (!CollectionUtils.isEmpty(newMemberList)) {
                List<GroupChat> groupChatList = new ArrayList<>(newMemberList.size());
                for (InviteParam inviteParam : newMemberList) {
                    GroupChat groupChat = new GroupChat(
                            idPoolService.nextChatId(ChatType.GROUP_CHAT),
                            inviteParam.getUid(), gid, null,
                            inviteParam.getUsername(), inviterId,
                            true, false,
                            new Date(), new Date());
                    groupChatList.add(groupChat);
                }
                groupChatMapper.insertBatch(groupChatList);
            }
        }
        // 新启动一个线程处理通知
        new Thread(() -> {
            List<GroupMsgRecord> successNotifyList = successFactory.done();
            if (!CollectionUtils.isEmpty(successNotifyList)) {
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
        String setKey = RedisKey.KET_GROUP_CHAT_MEMBERS + gid;
        if (!redisService.hasKey(setKey)) {
            loadGroupMemberListIntoRedis(gid);
        }
        boolean isMember = redisService.sHasKey(setKey, uid);
        return isMember;
    }

    @Override
    public List<GroupMemberView> getGroupMemberList(String uid, String gid) {
        List<GroupMemberView> groupMemberViewList = groupChatMapper.selectGroupMemberList(gid);
        for (GroupMemberView member : groupMemberViewList) {
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
        return groupMemberViewList;
    }

    @Override
    public Set<Object> getGroupMemberFromCache(String gid) {
        // 如果redis中没有，从数据库中加载到redis中
        if (!redisService.hasKey(gid)) {
            loadGroupMemberListIntoRedis(gid);
        }
        return redisService.setMembers(RedisKey.KET_GROUP_CHAT_MEMBERS + gid);
    }

    @Override
    @Transactional
    public void leaveGroupChat(String uid, String gid) {
        //0、 群主不能退群
        String ownerId = groupMapper.selectGroupOwnerId(gid);
        if (uid.equals(ownerId)) {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "群主退群前请先解散群聊");
        }
        //1、 删除用户-群关系
        GroupChat relation = groupChatMapper.findByUidGid(uid, gid);
        if (relation == null) {
            return;
        }
        groupChatMapper.softDeleteGroupMember(uid, gid);
        //2、 更新缓存
        redisService.setRemove(RedisKey.KET_GROUP_CHAT_MEMBERS + gid, uid);
        //3、 通知群主，xxx离开了群聊
        if (StringUtils.isNotEmpty(ownerId)) {
            Set<Object> destIdSet = new HashSet<>(1);
            destIdSet.add(ownerId);
            GroupMsgRecord notify = new GroupMsgRecord(idPoolService.nextChatId(ChatType.GROUP_CHAT), gid, gid,
                    4, relation.getUserAlias() + " 离开了群聊", "", true, new Date(), new Date());
            messageService.sendGroupNotify(destIdSet, gid, notify);
        }
    }

    @Override
    @Transactional
    public void kickOutGroupMember(List<GroupMemberView> memberList, String gid, String ownerId) {
        if (!checkIsGroupMember(ownerId, gid)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是群成员，或群已解散");
        }
        String realOwnerId = groupMapper.selectGroupOwnerId(gid);
        if (StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你不是群主");
        }
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        // 更新缓存
        Set<Object> allMember = getGroupMemberFromCache(gid);
        String key = RedisKey.KET_GROUP_CHAT_MEMBERS + gid;
        for (GroupMemberView member : memberList) {
            redisService.setRemove(key, member.getUid());
            factory.appendNotify(member.getGroupAlias() + " 被群主踢出了群聊");
        }
        // 更新数据库
        groupChatMapper.softDeleteGroupMemberBatch(memberList, gid);
        // 发送群通知
        List<GroupMsgRecord> notifyList = factory.done();
        messageService.sendGroupNotify(allMember, gid, notifyList);
    }

    @Override
    @Transactional
    public void dismissGroupChat(String ownerId, String gid) {
        String realOwnerId = groupMapper.selectGroupOwnerId(gid);
        if (StringUtils.isEmpty(realOwnerId) || !ownerId.equals(realOwnerId)) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你不是群主");
        }
        Set<Object> allMember = getGroupMemberFromCache(gid);
        // 群通知
        GroupNotifyFactory factory = GroupNotifyFactory.getInstance(gid);
        factory.appendNotify("该群已被群主解散");

        // 数据库
        groupChatMapper.softDeleteAllMember(gid);
        groupMapper.delete(gid);

        // 缓存删除
        redisService.del(RedisKey.KET_GROUP_CHAT_MEMBERS + gid);

        // 发送群通知
        List<GroupMsgRecord> notify = factory.done();
        messageService.sendGroupNotify(allMember, gid, notify);
    }

    @Override
    public void changeUserGroupAlias(String uid, String gid, String alias) {
        if (StringUtils.isEmpty(alias) || alias.length() >= 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群昵称长度不合法");
        }
        GroupChat relation = groupChatMapper.findByUidGid(uid, gid);
        if (relation == null) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "你还不是群成员");
        }
        relation.setUserAlias(alias);
        groupChatMapper.update(relation);
    }

    @Override
    public Map<String, String> updateGroupInfo(MultipartFile multipartFile, String groupName, String gid, String uid) {
        Map<String, String> result = new HashMap<>(3);
        Group group = new Group();
        group.setGid(gid);
        if (multipartFile != null) {
            String url = fileService.uploadAvatar(multipartFile, gid);
            result.put("groupAvatar", GlobalConst.Path.AVATAR_URL + url);
            group.setAvatar(url);
        }
        if (StringUtils.isNotEmpty(groupName) && groupName.length() <= 10) {
            group.setGroupName(groupName);
            result.put("groupName", groupName);
        } else {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "群名长度应在1-10个字符之间");
        }
        groupMapper.update(group);
        return result;
    }

    @Override
    public String getGidFromChatId(Long chatId) {
        String key = RedisKey.KEY_CHAT_GID_MAP + chatId.toString();
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

    @Override
    public Map<String, Object> openGroupChat(String uid, String gid) {
        GroupChat relation = groupChatMapper.findByUidGid(uid, gid);
        if (relation == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID);
        }
        Boolean isNewTalk = !relation.getShouldDisplay();
        if (isNewTalk) {
            relation.setShouldDisplay(true);
            relation.setUpdateTime(new Date());
            groupChatMapper.update(relation);
        }
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, uid + gid, false);
        Map<String, Object> result = new HashMap<>(2);
        result.put("isNewTalk", isNewTalk);
        result.put("relation", relation);
        return result;
    }

    private void loadGroupMemberListIntoRedis(String gid) {
        Set<String> groupMemberIdSet = groupChatMapper.selectGroupMemberIdSet(gid);
        if (groupMemberIdSet != null && groupMemberIdSet.size() > 0) {
            String setKey = RedisKey.KET_GROUP_CHAT_MEMBERS + gid;
            redisService.del(setKey);
            redisService.sSetAndTime(setKey, 2 * 60 * 60L, groupMemberIdSet.toArray());
        }
    }


}
