package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 群聊相关的业务逻辑接口
 */
@Service
public interface IGroupChatService {
    /**
     * 新建群聊
     * @param ownerId
     * @param groupName
     * @return
     */
    CreateGroupDTO createNewGroupChat(String ownerId, String groupName);

    /**
     * 保存群聊天记录
     * @param msg
     */
    void saveGroupChatMsgRecord(SendMsgDTO msg);
    void saveGroupChatMsgRecord(List<GroupMsgRecord> msgRecordList);
    void saveGroupChatMsgRecord(GroupMsgRecord groupMsgRecord);

    /**
     * 更新群聊最后一条记录信息
     * @param gid
     * @param lastMsgId
     * @param lastSenderId
     */
    void updateGroupLastMsg(String gid, Long lastMsgId, String lastSenderId);
    /**
     * 获取群列表
     * @param uid
     * @return
     */
    List<GroupChatInfoDTO> getGroupChatList(String uid);


    Map<String, ChatSession> getAllGroupChatSession(String gid);

    /**
     * 批量拉取好友入群
     * @param inviterId
     * @param gid
     * @param inviteDTOList
     * @return
     */
    boolean inviteFriendToChatGroup(String inviterId, String gid, List<InviteDTO> inviteDTOList) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

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
    List<GroupMemberDTO> getGroupMemberList(String uid, String gid);

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

    void kickOutGroupMember(List<GroupMemberDTO> memberIdList, String gid, String ownerId);

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
}
