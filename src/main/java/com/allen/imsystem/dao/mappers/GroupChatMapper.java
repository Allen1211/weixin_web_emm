package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupChat;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import com.allen.imsystem.model.pojo.UserChatGroup;
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

    Boolean selectGroupChatStatus(String gid);

    String selectGroupOwnerId(String gid);

    String selectUnUsedGid();

    String selectGidFromChatId(Long chatId);

    UserChatGroup selectUserChatGroupRelation(@Param("uid") String uid, @Param("gid") String gid);

    UserChatGroup selectUserChatGroupRelationByChatId(@Param("chatId") Long chatId);

    Set<String> selectGroupMemberIdSet(String gid);

    List<GroupChatInfoDTO> selectGroupChatList(String uid);

    List<GroupMemberDTO> selectGroupMemberList(String gid);

    @MapKey("myId")
    Map<String, ChatSessionDTO> selectGroupAllChatData(String gid);

    @MapKey("uid")
    Map<String, UserChatGroup> selectUserChatGroupRelationByUidList(@Param("list") List<InviteDTO> list, @Param("gid") String gid);

    ChatSessionDTO selectOneGroupChatData(Long chatId);

    Integer softDeleteUsedGid(String gid);

    Integer insertNewGroupChat(GroupChat groupChat);

    Integer insertUserChatGroup(UserChatGroup userChatGroup);

    Integer insertNewGroupMsgRecord(GroupMsgRecord groupMsgRecord);

    Integer insertNewGroupMsgRecordBatch(@Param("msgList") List<GroupMsgRecord> msgList);

    Integer insertUserChatGroupBatch(@Param("friendList") List<InviteDTO> friendList,
                                     @Param("relation") UserChatGroup relation);

    Integer updateGroupLastMsg(@Param("gid") String gid, @Param("lastMsgId") String lastMsgId, @Param("lastSenderId") String lastSenderId);

    Integer updateUserGroupChat(UserChatGroup userChatGroup);

    Integer updateGroupChat(GroupChat groupChat);

    Integer reActivateRelation(@Param("list") List<InviteDTO> list, @Param("gid") String gid);

    Integer softDeleteGroupMember(@Param("uid") String uid, @Param("gid") String gid);

    Integer softDeleteGroupMemberBatch(@Param("memberList") List<GroupMemberDTO> memberList, @Param("gid") String gid);

    Integer softDeleteAllMember(String gid);

    Integer softDeleteGroupChat(String gid);


    List<UserChatGroup> fix();
}
