package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.GroupChat;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import com.allen.imsystem.model.pojo.UserChatGroup;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupChatMapper {

    String selectGroupOwnerId(String gid);

    String selectUnUsedGid();

    UserChatGroup selectUserChatGroupRelation(@Param("uid")String uid,@Param("gid")String gid);

    UserChatGroup selectUserChatGroupRelationByChatId(@Param("chatId")String chatId);

    Set<String> selectGroupMemberIdSet(String gid);

    List<GroupChatInfoDTO> selectGroupChatList(String uid);

    List<GroupMemberDTO> selectGroupMemberList(String gid);

    @MapKey("myId")
    Map<String, ChatSessionDTO> selectGroupAllChatData(String gid);

    Integer softDeleteUsedGid(String gid);

    Integer insertNewGroupChat(GroupChat groupChat);

    Integer insertUserChatGroup(UserChatGroup userChatGroup);

    Integer insertNewGroupMsgRecord(GroupMsgRecord groupMsgRecord);

    Integer insertNewGroupMsgRecordBatch(@Param("msgList") List<GroupMsgRecord> msgList);

    Integer insertUserChatGroupBatch(@Param("friendList") List<InviteDTO> friendList,
                                     @Param("relation")UserChatGroup relation);

    Integer updateGroupLastMsg(@Param("gid")String gid, @Param("lastMsgId")String lastMsgId,@Param("lastSenderId")String lastSenderId);

    Integer updateUserGroupChat(UserChatGroup userChatGroup);

    Integer softDeleteGroupMember(@Param("uid")String uid, @Param("gid")String gid);

    Integer softDeleteGroupMemberBatch(@Param("memberList")List<GroupMemberDTO> memberList, @Param("gid")String gid);

    Integer softDeleteAllMember(String gid);

    Integer softDeleteGroupChat(String gid);
}
