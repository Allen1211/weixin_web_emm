package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.ApplyNotify;
import com.allen.imsystem.model.pojo.FriendApply;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.FriendRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface FriendMapper {

    List<String> selectFriendId(String uid);

    Set<String> selectTwoWayFriendId(String uid);

    List<String> selectAquiredId(String uid);

    Integer selectGroupId(@Param("uid")String uid, @Param("groupName")String groupName);

    Integer  selectApplyGroupId(@Param("fromUid") String fromUid, @Param("toUid") String toUid);

    FriendApply selectFriendApply(@Param("fromUid")String fromUid, @Param("toUid")String toUid);

    Boolean isGroupValid(Integer groupId);

    List<FriendApplicationDTO> selectLatestApply(@Param("uid") String uid,@Param("limit") Integer limit);

    Set<UserInfoDTO> selectFriendList(@Param("uid")String uid);

    List<UserInfoDTO> selectFriendListOrderByGroupId(@Param("uid")String uid);

    List<FriendInfoForInvite> selectFriendListForInvite(String uid);

    UserInfoDTO selectFriendInfo(@Param("friendId")String friendId);

    FriendRelation selectFriendRelation(@Param("uid")String uid, @Param("friendId")String friendId);

    FriendGroupPojo selectUserDefaultFriendGroup(@Param("uid")String uid);

    Integer checkIsFriend(@Param("uid")String uid, @Param("friendId")String friendId);

    Integer addFriendApply(FriendApply friendApply);

    Integer selectGroupSize(@Param("groupId") Integer groupId,@Param("uid") String uid);

    Integer updateFriendApplyPass(@Param("pass") boolean pass,@Param("fromUid") String fromUid,@Param("toUid") String toUid);

    Integer insertNewFriend(@Param("uidA") String aUid,@Param("uidB") String bUid,
                            @Param("abGroupId") Integer abGroupId,@Param("baGroupId") Integer baGroupId);

    List<FriendGroup> selectFriendGroupListWithSize(@Param("uid")String uid);

    Integer insertNewFriendGroup(@Param("uid")String uid,
                                 @Param("groupName")String groupName,@Param("isDefault")Boolean isDefault);

    Integer sortDeleteFriendA2B(@Param("uid")String uid, @Param("friendId")String friendId);

    Integer sortDeleteFriendB2A(@Param("uid")String uid, @Param("friendId")String friendId);

    Integer deleteFriend(@Param("uid")String uid, @Param("friendId")String friendId);

    Integer deleteFriendGroup(@Param("groupId") Integer groupId,@Param("uid") String uid);

    Integer updateFriendGroupName(@Param("groupId")Integer groupId, @Param("groupName")String groupName,
                                  @Param("uid")String uid);

    Integer moveGroupFriendToDefaultGroup(@Param("defaultGroupId")Integer defaultGroupId,@Param("groupId") Integer groupId,@Param("uid") String uid);

    Integer moveFriendToAnotherGroup(@Param("uid")String uid, @Param("friendId")String friendId,
                                     @Param("oldGroupId")Integer oldGroupId, @Param("newGroupId")Integer newGroupId);


    List<FriendRelation> selectAllFriendRelation();

    Integer updateFriendRelation(FriendRelation friendRelation);

    Integer deleteFriendApply(@Param("fromUid")String fromUid,@Param("toUid")String toUid);


}
