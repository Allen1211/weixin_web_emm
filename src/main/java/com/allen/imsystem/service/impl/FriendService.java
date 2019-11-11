package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.FriendMapper;
import com.allen.imsystem.dao.mappers.SearchMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.*;
import com.allen.imsystem.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FriendService implements IFriendService {


    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private IChatService chatService;

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IGroupChatService groupChatService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private INotifyService notifyService;

    @Override
    public List<UserSearchResult> searchStranger(String uid, String keyword) {
        Map<String, UserSearchResult> map = searchMapper.search(keyword);
        if (map == null){
            return new ArrayList<>();
        }
        List<String> friendId = friendMapper.selectFriendId(uid);   // 所有好友的id
        List<String> requiredId = friendMapper.selectAquiredId(uid);    // 所有已发送申请的用户id
        for (String id : friendId) {
            UserSearchResult result = map.get(id);
            if (result != null) {
                result.setApplicable(false);
                result.setReason("已添加");
                map.put(id, result);
            }
        }
        for (String id : requiredId) {
            UserSearchResult result = map.get(id);
            if (result != null) {
                result.setApplicable(false);
                result.setReason("已申请");
                map.put(id, result);
            }
        }
        UserSearchResult result = map.get(uid);
        if (result != null) {
            result.setApplicable(false);
            result.setReason("是自己");
            map.put(uid, result);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public Boolean checkIsMyFriend(String uid, String friendId) {
        boolean isFriend = redisService.sHasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid, friendId);
        if(isFriend){
            return true;
        }
        FriendRelation friendRelation = friendMapper.selectFriendRelation(uid, friendId);
        if (friendRelation == null) {
            return false;
        }
        if (friendRelation.getUidA().equals(uid)) {
            boolean deleteIt = friendRelation.getADeleteB();
            return !deleteIt;
        } else {
            boolean deleteIt = friendRelation.getBDeleteA();
            return !deleteIt;
        }
    }

    @Override
    public Boolean checkIsTwoWayFriend(String uid, String friendId) {
        if (!redisService.hasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid)) {
            loadFriendListIntoRedis(uid);
        }
        return redisService.sHasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid, friendId);
    }

    @Override
    public Boolean checkIsDeletedByFriend(String uid, String friendId) {
        // 先从缓存读取，缓存没有的话，再到数据库读取，并把读取出来的数据填入缓存中
        if (!redisService.hasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid)) {
            loadFriendListIntoRedis(uid);
        }
        boolean isFriend = redisService.sHasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid, friendId);
        return !isFriend;
    }

    @Override
    @Transactional
    public boolean addFriendApply(ApplyAddFriendDTO params, String uid) {
        String fromUId = uid;
        String toUId = params.getFriendId();
        String reason = params.getApplicationReason();
        if (reason == null) reason = "";

        // 删掉旧申请
        friendMapper.deleteFriendApply(fromUId,toUId);
        Integer groupId = null;

        if (params.getGroupId() == null) {
            FriendGroupPojo defaultGroup = friendMapper.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        } else {
            groupId = Integer.valueOf(params.getGroupId());
        }
        FriendApply friendApply = new FriendApply(fromUId,toUId,groupId,reason);
        boolean insertSuccess = friendMapper.addFriendApply(friendApply) > 0;
        if(insertSuccess){
            // 新启动一个线程推送通知
            String finalReason = reason;
            new Thread(()->{
                Integer applyId = friendApply.getId();
                ApplyNotify applyNotify = new ApplyNotify(params.getFriendId(),applyId,GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
                notifyService.saveNewApplyNotify(applyNotify);

                // 推送 新申请
                if(userService.isOnline(params.getFriendId())){
                    FriendApplicationDTO notify = new FriendApplicationDTO();
                    UserInfoDTO applicantInfo = friendMapper.selectFriendInfo(uid);
                    notify.setApplicantInfo(applicantInfo);
                    notify.setApplicationReason(finalReason);
                    notify.setHasAccept(false);
                    List<FriendApplicationDTO> notifyList = new ArrayList<>(1);
                    notifyList.add(notify);
                    messageService.sendNotify(204,params.getFriendId(),notifyList);
                }
            }).start();
        }
        return insertSuccess;
    }

    @Override
    @Transactional
    public boolean passFriendApply(String uid, String friendId, Integer groupId) {
        // 1 查询对方要把ta放到什么组
        FriendApply friendApply = friendMapper.selectFriendApply(friendId,uid);
        if(friendApply == null){
//            throw new BusinessException(ExceptionType.APPLY_HAS_BEEN_HANDLER,"没有未处理的申请");
            return true;
        }
        Integer bePutInGroupId = friendApply.getGroupId();

        // 如果没有设定组，则默认放入默认组
        if (groupId == null) {
            FriendGroupPojo defaultGroup = friendMapper.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        }
        // 2 更新用户申请表，将对方对当前用户的申请通过，同时也把当前用户对对方的申请全部通过
        boolean successUpdate = friendMapper.updateFriendApplyPass(true, friendId, uid) > 0
                | friendMapper.updateFriendApplyPass(true, uid, friendId) > 0;
        if (!successUpdate) { // 如果更新行数为0，说明不存在此申请或者申请已经被同意
            throw new BusinessException(ExceptionType.APPLY_HAS_BEEN_HANDLER);
        }

        // 3.5 判定对方是否已经是自己的好友，如果是，删掉原来的关系，再执行插入关系。若不是，直接执行插入
        boolean isMyFriend = checkIsMyFriend(uid, friendId);
        if (isMyFriend) {   // 原来已经是自己的好友了，删掉原来的好友关系
            friendMapper.deleteFriend(uid, friendId);
        }else{// 新的好友，如果他们之间还不存在会话，则新建一个新的会话
            String uidA = getUidAUidB(uid,friendId)[0];
            String uidB = getUidAUidB(uid,friendId)[1];
            PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA,uidB);
            if(privateChat == null){
                new Thread(() -> {
                    chatService.initNewPrivateChat(uid, friendId, false);
                }).start();
            }
        }

        // 4 插入好友表， 防止重复，限定uid小的作为uid_a,uid大的作为uid_b
        boolean successInsert = false;
        if (uid.compareTo(friendId) < 0) {
            successInsert = friendMapper.insertNewFriend(uid, friendId, bePutInGroupId, groupId) > 0;
        } else if (uid.compareTo(friendId) > 0) {
            successInsert = friendMapper.insertNewFriend(friendId, uid, groupId, bePutInGroupId) > 0;
        } else {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "不能添加自己为好友");
        }

        // 5 更新缓存
        addFriendIntoRedis(uid, friendId);
        addFriendIntoRedis(friendId, uid);

        // 6 新好友通知 双向推送
        if(successInsert){
            Integer finalGroupId = groupId;
            new Thread(()->{
                ApplyNotify applyNotifySelf = new ApplyNotify(uid,friendApply.getId(),GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
                ApplyNotify applyNotifyFriend = new ApplyNotify(friendId,friendApply.getId(),GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
                notifyService.saveNewApplyNotify(applyNotifySelf);
                notifyService.saveNewApplyNotify(applyNotifyFriend);
                // 给好友推送
                if(userService.isOnline(friendId)){
                    UserInfoDTO friendInfo = friendMapper.selectFriendInfo(uid);
                    NewFriendNotify notify = new NewFriendNotify(friendInfo,bePutInGroupId);
                    messageService.sendNotify(205,friendId,notify);
                }
                // 给自己推送
                if(userService.isOnline(uid)){
                    UserInfoDTO friendInfo = friendMapper.selectFriendInfo(friendId);
                    NewFriendNotify notify = new NewFriendNotify(friendInfo, finalGroupId);
                    messageService.sendNotify(205,uid,notify);
                }
            }).start();
        }
        return successInsert && successUpdate;
    }

    @Override
    public List<FriendApplicationDTO> getFriendApplicationList(String uid) {
        List<FriendApplicationDTO> friendApplicationDTOList = friendMapper.selectLatestApply(uid, 50);
        return friendApplicationDTOList;
    }

    @Override
    public List<FriendGroup> getFriendGroupList(String uid) {
        return friendMapper.selectFriendGroupListWithSize(uid);
    }

    @Override
    public Map<String,Object> getFriendListByGroup(String uid) {
        // 默认组的id
        Integer defaultGroupId = null;
        // 按组id升序排列的 好友列表
        List<UserInfoDTO> friendListOrderByGroup = friendMapper.selectFriendListOrderByGroupId(uid);
        // 按组id升序排列的 分组列表
        List<FriendGroup> friendGroupList = friendMapper.selectFriendGroupListWithSize(uid);
        // 按组id升序排列的 分组好友列表
        List<FriendListByGroupDTO> resultList = new ArrayList<>(friendGroupList.size());
        int begin = 0;
        for (FriendGroup friendGroup : friendGroupList) {
            FriendListByGroupDTO dto = new FriendListByGroupDTO();
            dto.setGroupId(friendGroup.getGroupId());
            dto.setGroupName(friendGroup.getGroupName());
            dto.setGroupSize(friendGroup.getGroupSize());
            dto.setIsDefault(friendGroup.getIsDefault());
            // 根据每一个组的大小
            int groupSize = friendGroup.getGroupSize();

            if (groupSize != 0) {
                dto.setMembers(friendListOrderByGroup.subList(begin, begin + friendGroup.getGroupSize()));
                begin += friendGroup.getGroupSize();
            } else {
                dto.setMembers(new ArrayList<>());
            }
            resultList.add(dto);

            if(friendGroup.getIsDefault()){
                defaultGroupId = friendGroup.getGroupId();
            }
        }
        Map<String,Object> resultMap = new HashMap<>(2);
        resultMap.put("groupList",resultList);
        resultMap.put("defaultGroupId",defaultGroupId);
        return resultMap;
    }

    @Override
    public Integer addFriendGroup(String uid, String groupName, Boolean isDefault) {
        if (groupName == null) {
            throw new BusinessException(ExceptionType.MISSING_PARAMETER_ERROR);
        }
        if (groupName.length() > 10)
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名长度应小于10");
        boolean groupNameHasExist = friendMapper.selectGroupId(uid,groupName) !=null ;
        if(groupNameHasExist){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"组名重复");
        }
        Integer affect = friendMapper.insertNewFriendGroup(uid, groupName, isDefault);
        Integer groupId = 0;
        if (affect > 0) {
            groupId = friendMapper.selectGroupId(uid, groupName);
        }
        return groupId;
    }

    @Override
    public Set<UserInfoDTO> getFriendList(String uid) {
        return friendMapper.selectFriendList(uid);
    }

    @Override
    public List<FriendInfoForInvite> getFriendListForInvite(String uid, String gid) {
        List<FriendInfoForInvite> resultList = friendMapper.selectFriendListForInvite(uid);
        for(FriendInfoForInvite friend : resultList){
            if(groupChatService.checkIsGroupMember(friend.getFriendInfo().getUid(),gid)){
                friend.setCanInvite(false);
                friend.setReason("已是群成员");
            }else if(checkIsDeletedByFriend(uid,friend.getFriendInfo().getUid())){
                friend.setCanInvite(false);
                friend.setReason("非好友");
            }else {
                friend.setCanInvite(true);
            }
        }
        return resultList;
    }

    @Override
    public UserInfoDTO getFriendInfo(String uid, String friendId) {
        boolean isNotFriend = friendMapper.checkIsFriend(uid, friendId) == 0;
        if (isNotFriend) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "这不是你的好友");
        }
        return friendMapper.selectFriendInfo(friendId);
    }

    @Override
    @Transactional
    public boolean deleteFriend(String uid, String friendId) {
        // 1、检查是否已经被好友删除
        Boolean isDeletedByFriend = checkIsDeletedByFriend(uid, friendId);
        if (isDeletedByFriend)//如果已经被对方删除，则执行物理删除
        {
            //物理删除好友关系
            friendMapper.deleteFriend(uid, friendId);
        } else {   // 否则执行逻辑删除
            sortDeleteFriend(uid, friendId);
        }
        // 移除掉与好友的会话
        chatService.removePrivateChat(uid, friendId);

        // 2、更新缓存
        removeFriendFromRedis(uid, friendId);
        removeFriendFromRedis(friendId, uid);
        return true;
    }

    private boolean sortDeleteFriend(String uid, String friendId) {
        if (uid.compareTo(friendId) < 0) {
            return friendMapper.sortDeleteFriendA2B(uid, friendId) > 0;
        } else {
            return friendMapper.sortDeleteFriendB2A(uid, friendId) > 0;
        }
    }

    @Override
    public boolean updateFriendGroupName(Integer groupId, String groupName, String uid) {
        if (StringUtils.isEmpty(groupName) ) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名不能为空");
        }
        if (StringUtils.length(groupName) > 10){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"好友分组名长度不得超过10个字符");
        }
        return friendMapper.updateFriendGroupName(groupId, groupName, uid) > 0;
    }

    @Override
    @Transactional
    public boolean deleteFriendGroup(Integer groupId, String uid) {
        // 1 判断该分组下是否有好友，如果没有直接删除组，结束
        Integer size = friendMapper.selectGroupSize(groupId, uid);
        // 2 获取该用户的默认分组，若删除的是默认分组，报错。
        FriendGroupPojo defaultGroup = friendMapper.selectUserDefaultFriendGroup(uid);
        if (groupId.equals(defaultGroup.getGroupId())) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "不能删除默认分组");
        }
        boolean moveSuccess = true;
        if (size > 0) {// 若有好友，将该分组下所有好友转至默认分组
            Integer affect = friendMapper.moveGroupFriendToDefaultGroup(defaultGroup.getGroupId(), groupId, uid);
            System.out.println(affect);
            moveSuccess = friendMapper.moveGroupFriendToDefaultGroup(defaultGroup.getGroupId(), groupId, uid) > 0;
        }
        // 3 删除掉该分组
        boolean deleteSuccess = friendMapper.deleteFriendGroup(groupId, uid) > 0;

        return moveSuccess && deleteSuccess;
    }

    @Override
    @Transactional
    public boolean moveFriendToOtherGroup(String uid, String friendId, Integer oldGroupId, Integer newGroupId) {
        Boolean isGroupValid = friendMapper.isGroupValid(newGroupId);
        if (isGroupValid == null || isGroupValid.booleanValue() == false) {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "要移动到的组不存在或已被删除");
        }
        boolean isSuccess = friendMapper.moveFriendToAnotherGroup(uid, friendId, oldGroupId, newGroupId) > 0;
        return isSuccess;
    }

    /**
     * 从redis中加载好友列表到redis中
     */
    private Long loadFriendListIntoRedis(String uid) {
        Set<String> twoWayFriendIdList = friendMapper.selectTwoWayFriendId(uid);
        if (twoWayFriendIdList != null) {
            return redisService.sSetAndTime(GlobalConst.Redis.KEY_FRIEND_SET + uid, 60 * 60L, twoWayFriendIdList.toArray());
        }
        return 0L;
    }

    /**
     * 添加一个好友到redis中某用户的好友列表（如果存在缓存）
     */
    private boolean addFriendIntoRedis(String uid, String friendId) {
        if (redisService.hasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid)) {
            return redisService.sSet(GlobalConst.Redis.KEY_FRIEND_SET + uid, friendId) > 0L;
        }
        return false;
    }
    /**
     * 从redis缓存中移除一个好友（如果存在缓存）
     */
    private boolean removeFriendFromRedis(String uid, String friendId) {
        if (redisService.hasKey(GlobalConst.Redis.KEY_FRIEND_SET + uid)) {
            return redisService.setRemove(GlobalConst.Redis.KEY_FRIEND_SET + uid, friendId) > 0L;
        }
        return false;
    }

    private String[] getUidAUidB(String uid, String friendId) {
        String uidA = uid.compareTo(friendId) < 0 ? uid : friendId;
        String uidB = uid.compareTo(friendId) < 0 ? friendId : uid;
        return new String[]{uidA, uidB};
    }
}
