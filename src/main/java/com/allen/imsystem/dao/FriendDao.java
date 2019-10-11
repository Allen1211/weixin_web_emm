package com.allen.imsystem.dao;

import com.allen.imsystem.dao.mappers.FriendMapper;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.FriendGroup;
import com.allen.imsystem.model.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class FriendDao {
    @Autowired
    private FriendMapper friendMapper;

    public List<String> getAllFriendId(String uid){
        return friendMapper.selectFriendId(uid);
    }

    public List<String> getAllRequiredToId(String uid){
        return friendMapper.selectAquiredId(uid);
    }

    public Integer selectGroupId(@Param("uid")String uid, @Param("groupName")String groupName){
        return friendMapper.selectGroupId(uid,groupName);
    }

    public Integer selectApplyGruopId(String fromUid, String toUid){
        return friendMapper.selectApplyGroupId(fromUid,toUid);
    }

    public List<FriendApplicationDTO> selectLatestApply(String uid, Integer limit){
        return friendMapper.selectLatestApply(uid,limit);
    }

    public Set<UserInfoDTO> selectFriendList(String uid){
        return friendMapper.selectFriendList(uid);
    }

    public UserInfoDTO selectFriendInfo(String friendId){
        return friendMapper.selectFriendInfo(friendId);
    }

    public Integer checkIsFriend(String uid, String friendId){
        return friendMapper.checkIsFriend(uid,friendId);
    }

    public Integer selectGroupSize(Integer groupId,String uid){
        return friendMapper.selectGroupSize(groupId,uid);
    }

    public Integer deleteFriendGroup(Integer groupId,String uid){
        return friendMapper.deleteFriendGroup(groupId, uid);
    }

    public Integer addFriendApply(String fromUid,String toUid,Integer groupId,String reason){
        return friendMapper.addFriendApply(fromUid,toUid,groupId,reason);
    }

    public Integer updateFriendApplyPass(boolean pass, String fromUid,String toUid){
        return friendMapper.updateFriendApplyPass(pass,fromUid,toUid);
    }

    public Integer insertNewFriend(String aUid,String bUid, Integer abGroupId,Integer baGroupId){
        return friendMapper.insertNewFriend(aUid,bUid,abGroupId,baGroupId);
    }

    public List<FriendGroup> selectFriendGroupListWithSize(String uid){
        return friendMapper.selectFriendGroupListWithSize(uid);
    }

    public Integer insertNewFriendGroup(Integer userId,String uid, String groupName){
        return friendMapper.insertNewFriendGroup(userId,uid,groupName);
    }

    public Integer deleteFriend(String uid,String friendId){
        return friendMapper.deleteFriend(uid,friendId);
    }

    public Integer updateFriendGroupName(Integer groupId,String groupName,Integer userId){
        return friendMapper.updateFriendGroupName(groupId,groupName,userId);
    }

    public Integer moveGroupFriendToDefaultGroup(Integer groupId, String uid){
        return friendMapper.moveGroupFriendToDefaultGroup(groupId,uid);
    }

    public Integer moveFriendToAnotherGroup(String uid, String friendId,
                                            Integer oldGroupId, Integer newGroupId){
        return friendMapper.moveFriendToAnotherGroup(uid,friendId,oldGroupId,newGroupId);
    }
}
