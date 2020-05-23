package com.allen.imsystem.mappers;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.FriendRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@Mapper
public interface FriendMapper {

    List<String> selectFriendId(String uid);

    Set<String> selectTwoWayFriendId(String uid);

    Set<UserInfoDTO> selectFriendList(@Param("uid") String uid);

    List<UserInfoDTO> selectFriendListOrderByGroupId(@Param("uid") String uid);

    List<FriendInfoForInvite> selectFriendListForInvite(String uid);

    UserInfoDTO selectFriendInfo(@Param("friendId") String friendId);

    FriendRelation selectFriendRelation(@Param("uid") String uid, @Param("friendId") String friendId);

    Integer checkIsFriend(@Param("uid") String uid, @Param("friendId") String friendId);

    Integer insertNewFriend(@Param("uidA") String aUid, @Param("uidB") String bUid,
                            @Param("abGroupId") Integer abGroupId, @Param("baGroupId") Integer baGroupId);

    Integer sortDeleteFriendA2B(@Param("uid") String uid, @Param("friendId") String friendId);

    Integer sortDeleteFriendB2A(@Param("uid") String uid, @Param("friendId") String friendId);

    Integer deleteFriend(@Param("uid") String uid, @Param("friendId") String friendId);

    int insert(FriendRelation friendRelation);

    int updateById(FriendRelation friendRelation);

    List<FriendRelation> selectAll();
}
