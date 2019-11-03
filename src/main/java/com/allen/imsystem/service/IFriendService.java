package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public interface IFriendService {

    /**
     * 搜索陌生人
     * @param uid
     * @param keyword
     * @return
     */
    List<UserSearchResult> searchStranger(String uid, String keyword);

    /**
     * 检查对方是否是自己的好友
     * @param uid
     * @param friendId
     * @return
     */
    Boolean checkIsMyFriend(String uid, String friendId);

    Boolean checkIsTwoWayFriend(String uid, String friendId);

    /**
     * 检查对方是否已经把自己删除
     * @param uid
     * @param friendId
     * @return
     */
    Boolean checkIsDeletedByFriend(String uid,String friendId);

    /**
     * 申请加好友
     */
    boolean addFriendApply(ApplyAddFriendDTO params, String uid);

    /**
     * 通过好友申请
     */
    boolean passFriendApply(String uId, String friendId,Integer groupId);

    /**
     * 获取好友申请列表
     */
    List<FriendApplicationDTO> getFriendApplicationList (String uid);

    /**
     * 获取用户的好友分组列表
     */
    List<FriendGroup> getFriendGroupList(String uid);

    /**
     * 按分组获取好友列表
     */
     Map<String,Object> getFriendListByGroup(String uid);
    /**
     * 新建一个好友分组
     */
    Integer addFriendGroup(String uid, String groupName,Boolean isDefault);

    /**
     * 获取好友列表
     */
    Set<UserInfoDTO> getFriendList(String uid);

    List<FriendInfoForInvite> getFriendListForInvite(String uid,String gid);

    /**
     * 获取好友信息
     */
    UserInfoDTO getFriendInfo(String uid,String friendId);

    /**
     * 删除好友
     */
    boolean deleteFriend(String uid,String friendId);

    /**
     * 更改好友分组组名
     */
    boolean updateFriendGroupName(Integer groupId, String groupName,String uid);

    /**
     * 删除一个好友分组，内部如果有好友则全移到默认分组
     */
    boolean deleteFriendGroup(Integer groupId,String uid);

    /**
     * 移动好友到另一个分组
     */
    boolean moveFriendToOtherGroup(String uid,String friendId, Integer oldGroupId, Integer newGroupId);
}
