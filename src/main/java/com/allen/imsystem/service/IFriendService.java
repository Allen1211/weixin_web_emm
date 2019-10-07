package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ApplyAddFriendDTO;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.FriendGroup;
import com.allen.imsystem.model.dto.UserSearchResult;
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
    boolean addFriendApply(ApplyAddFriendDTO params);

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
}
