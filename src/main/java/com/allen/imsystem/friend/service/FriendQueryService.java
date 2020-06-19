package com.allen.imsystem.friend.service;

import com.allen.imsystem.friend.model.vo.FriendInfoForInvite;
import com.allen.imsystem.friend.model.vo.UserSearchResult;
import com.allen.imsystem.user.model.vo.UserInfoView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 好友查询相关业务逻辑的接口
 */
public interface FriendQueryService {

    /**
     * 搜索陌生人
     *
     * @param uid
     * @param keyword
     * @return
     */
    List<UserSearchResult> searchStranger(String uid, String keyword);

    /**
     * 检查对方是否是自己的好友
     *
     * @param uid
     * @param friendId
     * @return
     */
    boolean checkIsMyFriend(String uid, String friendId);

    boolean checkIsTwoWayFriend(String uid, String friendId);

    /**
     * 检查对方是否已经把自己删除
     *
     * @param uid
     * @param friendId
     * @return
     */
    boolean checkIsDeletedByFriend(String uid, String friendId);

    /**
     * 按分组获取好友列表
     */
    Map<String, Object> getFriendListByGroup(String uid);

    /**
     * 获取好友列表
     */
    Set<UserInfoView> getFriendList(String uid);

    List<FriendInfoForInvite> getFriendListForInvite(String uid, String gid);

    /**
     * 获取好友信息
     */
    UserInfoView getFriendInfo(String uid, String friendId);

}
