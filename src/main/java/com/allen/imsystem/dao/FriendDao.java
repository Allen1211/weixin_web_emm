package com.allen.imsystem.dao;

import com.allen.imsystem.dao.mappers.FriendMapper;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.FriendGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public Integer selectApplyGruopId(String fromUid, String toUid){
        return friendMapper.selectApplyGroupId(fromUid,toUid);
    }

    public List<FriendApplicationDTO> selectLatestApply(String uid, Integer limit){
        return friendMapper.selectLatestApply(uid,limit);
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
}
