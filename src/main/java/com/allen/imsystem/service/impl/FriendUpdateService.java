package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.mappers.*;
import com.allen.imsystem.model.dto.ApplyAddFriendDTO;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.UidABHelper;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.pojo.FriendApply;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @ClassName FriendApplyService
 * @Description 好友更新相关业务逻辑实现
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
@Service
public class FriendUpdateService implements IFriendUpdateService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendApplyMapper friendApplyMapper;

    @Autowired
    private FriendGroupMapper friendGroupMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private PrivateChatMapper privateChatMapper;

    @Autowired
    private IChatService chatService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendQueryService friendService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private INotifyService notifyService;


    /**
     * 申请加好友
     *
     * @param params 申请加好友的参数类
     * @param uid    用户uid
     */
    @Override
    public boolean addFriendApply(ApplyAddFriendDTO params, String uid) {
        String fromUId = uid;
        String toUId = params.getFriendId();
        String reason = params.getApplicationReason();
        if (reason == null) reason = "";

        // 删掉旧申请
        friendApplyMapper.deleteFriendApply(fromUId,toUId);
        Integer groupId = null;

        if (params.getGroupId() == null) {
            FriendGroupPojo defaultGroup = friendGroupMapper.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        } else {
            groupId = Integer.valueOf(params.getGroupId());
        }
        FriendApply friendApply = new FriendApply(fromUId,toUId,groupId,reason);
        boolean insertSuccess = friendApplyMapper.addFriendApply(friendApply) > 0;
        if(insertSuccess){
            // 新启动一个线程推送通知
            String finalReason = reason;
            new Thread(()->{
                notifyService.saveAndPushNewApplyNotify(uid,params.getFriendId(),finalReason,friendApply.getId());
            }).start();
        }
        return insertSuccess;
    }

    /**
     * 通过好友申请
     *
     * @param uid      用户uid
     * @param friendId 好友uid
     * @param groupId  要放入的分组id
     */
    @Override
    public boolean passFriendApply(String uid, String friendId, Integer groupId) {
        // 1 查询对方要把ta放到什么组
        FriendApply friendApply = friendApplyMapper.selectFriendApply(friendId,uid);
        if(friendApply == null){
            return true;
        }
        Integer bePutInGroupId = friendApply.getGroupId();

        // 如果没有设定组，则默认放入默认组
        if (groupId == null) {
            FriendGroupPojo defaultGroup = friendGroupMapper.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        }
        // 2 更新用户申请表，将对方对当前用户的申请通过，同时也把当前用户对对方的申请全部通过
        boolean successUpdate = friendApplyMapper.updateFriendApplyPass(true, friendId, uid) > 0
                | friendApplyMapper.updateFriendApplyPass(true, uid, friendId) > 0;
        if (!successUpdate) { // 如果更新行数为0，说明不存在此申请或者申请已经被同意
            throw new BusinessException(ExceptionType.APPLY_HAS_BEEN_HANDLER);
        }

        // 3.5 判定对方是否已经是自己的好友，如果是，删掉原来的关系，再执行插入关系。若不是，直接执行插入
        boolean isMyFriend = friendService.checkIsMyFriend(uid, friendId);
        if (isMyFriend) {   // 原来已经是自己的好友了，删掉原来的好友关系
            friendMapper.deleteFriend(uid, friendId);
        }else{// 新的好友，如果他们之间还不存在会话，则新建一个新的会话
            UidABHelper uidAB = UidABHelper.sortAndCreate(uid,friendId);
            PrivateChat privateChat = privateChatMapper.findByUidAB(uidAB.getUidA(),uidAB.getUidB());
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
                // 给好友推送
                notifyService.saveAndPushNewFriendNotify(friendId,friendApply.getId(),bePutInGroupId);
                // 给自己推送
                notifyService.saveAndPushNewFriendNotify(uid,friendApply.getId(),finalGroupId);
            }).start();
        }
        return successInsert;
    }

    /**
     * 获取好友申请列表
     *
     * @param uid 用户uid
     * @return 好友分组列表
     */
    @Override
    public List<FriendApplicationDTO> getFriendApplicationList(String uid) {
        List<FriendApplicationDTO> friendApplicationList = friendApplyMapper.selectLatestApply(uid, 50);
        for(FriendApplicationDTO friendApplication : friendApplicationList){
            UserInfoDTO userInfo = userService.findUserInfoDTO(friendApplication.getFromUid());
            friendApplication.setApplicantInfo(userInfo);
        }
        return friendApplicationList;
    }

    /**
     * 删除好友
     *
     * @param uid      用户uid
     * @param friendId 要删除的好友uid
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteFriend(String uid, String friendId) {
        // 1、检查是否已经被好友删除
        Boolean isDeletedByFriend = friendService.checkIsDeletedByFriend(uid, friendId);
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
}
