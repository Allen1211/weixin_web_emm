package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ApplyAddFriendDTO;
import com.allen.imsystem.model.dto.FriendApplicationDTO;

import java.util.List;

/**
 * @ClassName IFriendApplyService
 * @Description 好友申请相关业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
public interface IFriendUpdateService {

    /**
     * 申请加好友
     * @param params 申请加好友的参数类
     * @param uid 用户uid
     */
    boolean addFriendApply(ApplyAddFriendDTO params, String uid);

    /**
     * 通过好友申请
     * @param uId 用户uid
     * @param friendId 好友uid
     * @param groupId 要放入的分组id
     */
    boolean passFriendApply(String uId, String friendId, Integer groupId);

    /**
     * 获取好友申请列表
     * @param uid 用户uid
     * @return 好友分组列表
     */
    List<FriendApplicationDTO> getFriendApplicationList(String uid);


    /**
     * 删除好友
     * @param uid 用户uid
     * @param friendId 要删除的好友uid
     * @return 是否删除成功
     */
    boolean deleteFriend(String uid, String friendId);
}
