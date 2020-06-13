package com.allen.imsystem.friend.service.impl;

import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.friend.mappers.FriendApplyMapper;
import com.allen.imsystem.friend.mappers.FriendGroupMapper;
import com.allen.imsystem.friend.mappers.FriendMapper;
import com.allen.imsystem.friend.mappers.SearchMapper;
import com.allen.imsystem.friend.model.vo.FriendGroupView;
import com.allen.imsystem.friend.model.vo.FriendInfoForInvite;
import com.allen.imsystem.friend.model.vo.FriendListByGroupView;
import com.allen.imsystem.friend.model.vo.UserSearchResult;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.friend.model.pojo.FriendRelation;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.model.vo.UserInfoView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 好友查询相关业务逻辑实现
 */
@Service
public class FriendQueryServiceImpl implements FriendQueryService {

    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendApplyMapper friendApplyMapper;

    @Autowired
    private FriendGroupMapper friendGroupMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GroupChatService groupChatService;

    @Override
    public List<UserSearchResult> searchStranger(String uid, String keyword) {
        Map<String, UserSearchResult> map = searchMapper.search(keyword);
        if (map == null){
            return new ArrayList<>();
        }
        List<String> friendId = friendMapper.selectFriendId(uid);   // 所有好友的id
        List<String> requiredId = friendApplyMapper.selectAquiredId(uid);    // 所有已发送申请的用户id
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
        boolean isFriend = redisService.sHasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, friendId);
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
        if (!redisService.hasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid)) {
            loadFriendListIntoRedis(uid);
        }
        return redisService.sHasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, friendId);
    }

    @Override
    public Boolean checkIsDeletedByFriend(String uid, String friendId) {
        // 先从缓存读取，缓存没有的话，再到数据库读取，并把读取出来的数据填入缓存中
        if (!redisService.hasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid)) {
            loadFriendListIntoRedis(uid);
        }
        boolean isFriend = redisService.sHasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, friendId);
        return !isFriend;
    }

    @Override
    public Map<String,Object> getFriendListByGroup(String uid) {
        // 默认组的id
        Integer defaultGroupId = null;
        // 按组id升序排列的 好友列表
        List<UserInfoView> friendListOrderByGroup = friendMapper.selectFriendListOrderByGroupId(uid);
        // 按组id升序排列的 分组列表
        List<FriendGroupView> friendGroupViewList = friendGroupMapper.selectFriendGroupList(uid);
        // 按组id升序排列的 分组好友列表
        List<FriendListByGroupView> resultList = new ArrayList<>(friendGroupViewList.size());
        int begin = 0;
        for (FriendGroupView friendGroupView : friendGroupViewList) {
            FriendListByGroupView dto = new FriendListByGroupView();
            dto.setGroupId(friendGroupView.getGroupId());
            dto.setGroupName(friendGroupView.getGroupName());
            dto.setGroupSize(friendGroupView.getGroupSize());
            dto.setIsDefault(friendGroupView.getIsDefault());
            // 根据每一个组的大小
            int groupSize = friendGroupView.getGroupSize();

            if (groupSize != 0) {
                dto.setMembers(friendListOrderByGroup.subList(begin, begin + friendGroupView.getGroupSize()));
                begin += friendGroupView.getGroupSize();
            } else {
                dto.setMembers(new ArrayList<>());
            }
            resultList.add(dto);

            if(friendGroupView.getIsDefault()){
                defaultGroupId = friendGroupView.getGroupId();
            }
        }
        Map<String,Object> resultMap = new HashMap<>(2);
        resultMap.put("groupList",resultList);
        resultMap.put("defaultGroupId",defaultGroupId);
        return resultMap;
    }

    @Override
    public Set<UserInfoView> getFriendList(String uid) {
        return friendMapper.selectFriendList(uid);
    }

    @Override
    public List<FriendInfoForInvite> getFriendListForInvite(String uid, String gid) {
        List<FriendInfoForInvite> resultList = friendMapper.selectFriendListForInvite(uid);
        for(FriendInfoForInvite friend : resultList){
            if(groupChatService.checkIsGroupMember(friend.getFriendInfo().getUid(),gid)){
                friend.setCanInvite(false);
                friend.setReason("已是群成员");
            }else {
                friend.setCanInvite(true);
            }
        }
        return resultList;
    }

    @Override
    public UserInfoView getFriendInfo(String uid, String friendId) {
        boolean isNotFriend = friendMapper.checkIsFriend(uid, friendId) == 0;
        if (isNotFriend) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "这不是你的好友");
        }
        return friendMapper.selectFriendInfo(friendId);
    }

    /**
     * 从redis中加载好友列表到redis中
     */
    private Long loadFriendListIntoRedis(String uid) {
        Set<String> twoWayFriendIdList = friendMapper.selectTwoWayFriendId(uid);
        if (twoWayFriendIdList != null) {
            return redisService.sSetAndTime(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, 60 * 60L, twoWayFriendIdList.toArray());
        }
        return 0L;
    }

    /**
     * 添加一个好友到redis中某用户的好友列表（如果存在缓存）
     */
    private boolean addFriendIntoRedis(String uid, String friendId) {
        if (redisService.hasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid)) {
            return redisService.sSet(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, friendId) > 0L;
        }
        return false;
    }
    /**
     * 从redis缓存中移除一个好友（如果存在缓存）
     */
    private boolean removeFriendFromRedis(String uid, String friendId) {
        if (redisService.hasKey(GlobalConst.RedisKey.KEY_FRIEND_SET + uid)) {
            return redisService.setRemove(GlobalConst.RedisKey.KEY_FRIEND_SET + uid, friendId) > 0L;
        }
        return false;
    }
}
