package com.allen.imsystem.chat.service;

import com.allen.imsystem.chat.model.param.CreateGroupParam;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.friend.model.param.InviteParam;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.chat.model.vo.GroupView;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 群聊相关的业务逻辑接口
 */
@Service
public interface GroupChatService {
    /**
     * 新建群聊
     * @param ownerId 群主id
     * @param groupName 群名
     */
    GroupView createGroup(String ownerId, String groupName);

    /**
     * 保存群聊天记录
     * @param msg 消息
     */
    GroupMsgRecord saveGroupChatMsgRecord(SendMsgDTO msg);
    void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList);
    void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord);

    /**
     * 更新群聊最后一条记录信息
     * @param gid
     * @param lastMsgId
     * @param lastSenderId
     */
    void updateGroupLastMsg(String gid, Long lastMsgId,String lastMsgContent, Date lastMsgCreateTime,
                            String lastSenderId);

    /**
     * 获取群列表
     * @param uid
     * @return
     */
    List<GroupView> findGroupList(String uid);

    /**
     * 获取该群的所有群会话，以Map的形式返回，Key: 群会话所属的用户id
     * @param gid
     * @return
     */
    Map<String, ChatSession> getAllGroupChatSession(String gid);

    /**
     * 批量拉取好友入群
     * @param inviterId 邀请者uid
     * @param gid 群id
     * @param inviteParamList
     * @return
     */
    boolean inviteFriendToChatGroup(String inviterId, String gid, List<InviteParam> inviteParamList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    /**
     * 检查某用户是否是指定群的成员
     * @param uid
     * @param gid
     * @return
     */
    boolean checkIsGroupMember(String uid, String gid);

    /**
     * 获取群成员信息列表
     * @param uid
     * @param gid
     * @return
     */
    List<GroupMemberView> getGroupMemberList(String uid, String gid);

    /**
     * 获取群成员ID集合
     * @param gid
     * @return
     */
    Set<Object> getGroupMemberFromCache(String gid);

    /**
     * 成员退群
     * @param uid
     * @param gid
     */
    void leaveGroupChat(String uid, String gid);

    void kickOutGroupMember(List<GroupMemberView> memberIdList, String gid, String ownerId);

    /**
     * 群主解散群聊
     * @param ownerId
     * @param gid
     */
    void dismissGroupChat(String ownerId, String gid);

    /**
     * 群成员更改群名称
     * @param uid
     * @param alias
     */
    void changeUserGroupAlias(String uid, String gid, String alias);

    /**
     * 修改群信息
     * @param multipartFile
     * @param groupName
     * @param gid
     * @param uid
     * @return
     */
    Map<String,String> updateGroupInfo(MultipartFile multipartFile, String groupName, String gid, String uid);

    String getGidFromChatId(Long chatId);

    /**
     * 开启一个群聊会话
     * @param uid
     * @param gid
     * @return
     */
    Map<String, Object> openGroupChat(String uid, String gid);

}
