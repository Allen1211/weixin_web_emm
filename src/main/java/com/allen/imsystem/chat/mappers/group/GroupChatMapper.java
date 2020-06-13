package com.allen.imsystem.chat.mappers.group;

import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.friend.model.param.InviteParam;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.vo.GroupView;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@Mapper
public interface GroupChatMapper {

    /**
     * 查询user_chat_group的实体类
     * @param uid 用户id
     * @param gid 群id
     */
    GroupChat findByUidGid(@Param("uid") String uid, @Param("gid") String gid);

    /**
     * 查询user_chat_group的实体类
     * @param chatId 群聊会话id
     */
    GroupChat findByChatId(@Param("chatId") Long chatId);

    /**
     * 插入用户群聊会话关系
     * @param groupChat 用户群聊会话关系
     */
    int insert(GroupChat groupChat);

    /**
     * 插入用多个户群聊会话关系
     */
    int insertBatch(@Param("friendList") List<InviteParam> friendList,
                    @Param("relation") GroupChat relation);

    /**
     * 更新用户群聊会话关系
     */
    int update(GroupChat groupChat);

    /**
     * 查找群会话列表
     * @param uid 用户uid
     * @return 群会话列表
     */
    List<ChatSession> selectGroupChatList(String uid);

    /**
     * 查找一个群会话信息
     * @param chatId 会话id
     * @return 群会话信息
     */
    ChatSessionInfo selectGroupChatData(Long chatId);


    /**
     * 查询该群聊会话所对应的群id
     * @param chatId 群聊会话id
     * @return 群id
     */
    String selectGidFromChatId(Long chatId);

    /**
     * 查询某个群的群成员uid集合
     * @param gid 群id
     * @return 群成员uid集合
     */
    Set<String> selectGroupMemberIdSet(String gid);

    /**
     *
     * @param uid
     * @return
     */
    List<GroupView> findGroupViewListByUid(String uid);

    /**
     * 查询群成员列表
     * @param gid 群id
     * @return 群成员列表
     */
    List<GroupMemberView> selectGroupMemberList(String gid);

    @MapKey("myId")
    Map<String, ChatSession> selectGroupAllChatData(String gid);

    @MapKey("uid")
    Map<String, GroupChat> selectUserChatGroupRelationByUidList(@Param("list") List<InviteParam> list, @Param("gid") String gid);

    ChatSession selectOneGroupChatData(Long chatId);


    int reActivateRelation(@Param("list") List<InviteParam> list, @Param("gid") String gid);

    int softDeleteGroupMember(@Param("uid") String uid, @Param("gid") String gid);

    int softDeleteGroupMemberBatch(@Param("memberList") List<GroupMemberView> memberList, @Param("gid") String gid);

    int softDeleteAllMember(String gid);

}
