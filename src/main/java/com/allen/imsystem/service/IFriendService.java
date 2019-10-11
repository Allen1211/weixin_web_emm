package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.*;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.stereotype.Service;

import java.util.List;
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
     * 新建一个好友分组
     */
    Integer addFriendGroup(Integer userId, String uid, String groupName);

    /**
     * 获取好友列表
     */
    Set<UserInfoDTO> getFriendList(String uid);

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
    boolean updateFriendGroupName(Integer groupId, String groupName,Integer userId);

    /**
     * 删除一个好友分组，内部如果有好友则全移到默认分组
     */
    boolean deleteFriendGroup(Integer groupId,String uid);

    /**
     * 移动好友到另一个分组
     */
    boolean moveFriendToOtherGroup(String uid,String friendId, Integer oldGroupId, Integer newGroupId);
}
