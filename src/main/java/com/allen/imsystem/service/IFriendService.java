package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
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
    boolean addFriendGroup(String uid,String groupName);

    /**
     * 获取好友列表
     */
    List<UserInfoDTO> getFriendList(String uid);

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
    boolean updateFriendGroupName(Integer groupId, String groupName);
}
