package com.allen.imsystem.chat.service;

import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.chat.model.vo.GroupView;
import com.allen.imsystem.friend.model.param.InviteParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName GroupService
 * @Description 聊天群 有关业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/6/13
 * @Version 1.0
 */
public interface GroupService {
    /**
     * 获取群列表
     * @param uid 用户id
     * @return 群列表
     */
    List<GroupView> findGroupList(String uid);

    /**
     * 检查某用户是否是指定群的成员
     * @param uid 要检查的用户id
     * @param gid 群id
     * @return 是否是指定群的成员
     */
    boolean checkIsGroupMember(String uid, String gid);

    /**
     * 获取群成员ID集合
     * @param gid 群id
     * @return 群成员ID集合
     */
    Set<String> getGroupMemberFromCache(String gid);

    /**
     * 获取群成员列表
     * @param uid 用户id
     * @param gid 群id
     * @return 群成员列表
     */
    List<GroupMemberView> getGroupMemberList(String uid, String gid);

    /**
     * 新建群聊
     * @param ownerId 群主id
     * @param groupName 群名
     */
    GroupView createGroup(String ownerId, String groupName);

    /**
     * 批量拉取好友入群
     * @param inviterId 邀请者uid
     * @param gid 群id
     * @param inviteParamList 被邀请者列表
     * @return 是否成功
     */
    boolean inviteFriendToGroup(String inviterId, String gid, List<InviteParam> inviteParamList);

    /**
     * 成员退群
     * @param uid 要退群的用户id
     * @param gid 群id
     */
    void leaveGroupChat(String uid, String gid);

    /**
     * 群成员更改群名称
     * @param uid 要更改名称的群成员id
     * @param alias 要更改为的群名称
     */
    void changeUserGroupAlias(String uid, String gid, String alias);

    /**
     * 修改群信息
     * @param multipartFile 群头像
     * @param groupName 群名称
     * @param gid 群id
     * @param uid 更改者用户id
     * @return 新的群消息
     */
    Map<String,String> updateGroupInfo(MultipartFile multipartFile, String groupName, String gid, String uid);

    /************** 以下是群主才有权限使用的功能 ***************/

    /**
     * 踢出群成员
     * @param memberIdList 要踢出的群成员列表
     * @param gid 群id
     * @param ownerId 群主id
     */
    void kickOutGroupMember(List<GroupMemberView> memberIdList, String gid, String ownerId);

    /**
     * 群主解散群聊
     * @param ownerId 群主id
     * @param gid 群id
     */
    void dismissGroupChat(String ownerId, String gid);
}
