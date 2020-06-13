package com.allen.imsystem.friend.service;

import com.allen.imsystem.friend.model.vo.FriendGroupView;

import java.util.List;

/**
 * @ClassName FriendGroupService
 * @Description 好友分组相关业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
public interface FriendGroupService {

    /**
     * 获取用户的好友分组列表
     * @param uid 用户uid
     */
    List<FriendGroupView> getFriendGroupList(String uid);

    /**
     * 新建一个好友分组
     * @param uid 用户uid
     * @param groupName 分组名
     * @param isDefault 是否默认分组
     */
    Integer addFriendGroup(String uid, String groupName, Boolean isDefault);

    /**
     * 更改好友分组组名
     *
     * @param groupId 分组id
     * @param groupName 分组名
     * @param uid 用户uid
     */
    boolean updateFriendGroupName(Integer groupId, String groupName, String uid);

    /**
     * 删除一个好友分组，内部如果有好友则全移到默认分组
     *
     * @param groupId 分组id
     * @param uid 用户uid
     */
    boolean deleteFriendGroup(Integer groupId, String uid);

    /**
     * 移动好友到另一个分组
     *
     * @param uid 用户uid
     * @param friendId 好友uid
     * @param oldGroupId 旧分组id
     * @param newGroupId 新分组id
     */
    boolean moveFriendToOtherGroup(String uid, String friendId, Integer oldGroupId, Integer newGroupId);
}
