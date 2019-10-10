package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.dao.FriendDao;
import com.allen.imsystem.dao.SearchDao;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class FriendService implements IFriendService {

    @Autowired
    private SearchDao searchDao;

    @Autowired
    private FriendDao friendDao;

    @Autowired
    private UserDao userDao;

    @Override
    public List<UserSearchResult> searchStranger(String uid, String keyword) {
        Map<String, UserSearchResult> map = searchDao.searchUserByKeyword(keyword);
        if(map == null)
            new ArrayList<UserSearchResult>();
        List<String> friendId = friendDao.getAllFriendId(uid);
        List<String> requiredId = friendDao.getAllRequiredToId(uid);
        for(String id:friendId){
            UserSearchResult result = map.get(id);
            if(result!=null){
                result.setApplicable(false);
                result.setReason("已是好友");
                map.put(id,result);
            }
        }
        for(String id:requiredId){
            UserSearchResult result = map.get(id);
            if(result!=null){
                result.setApplicable(false);
                result.setReason("已申请");
                map.put(id,result);
            }
        }
        UserSearchResult result = map.get(uid);
        if(result!=null){
            result.setApplicable(false);
            result.setReason("是自己");
            map.put(uid,result);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    @Transactional
    public boolean addFriendApply(ApplyAddFriendDTO params,String uid) {
        String fromUId = uid;
        String toUId = params.getFriendId();
        String reason = params.getApplicationReason();
        Integer groupId = params.getGroupId()==null? 1:Integer.valueOf(params.getGroupId()) ;
        return friendDao.addFriendApply(fromUId,toUId,groupId,reason) > 0;
    }

    @Override
    @Transactional
    public boolean passFriendApply(String uid, String friendId, Integer groupId) {
        // 1 更新用户申请表，将pass改成1
        boolean successUpdate = friendDao.updateFriendApplyPass(true,friendId,uid) > 0;
        // 2 查询对方要把ta放到什么组
        Integer bePutInGroupId = friendDao.selectApplyGruopId(friendId,uid);
        // 2 插入好友表
        boolean successInsert = friendDao.insertNewFriend(uid,friendId,bePutInGroupId,groupId) > 0;
        return successInsert&&successUpdate;
    }

    @Override
    public List<FriendApplicationDTO> getFriendApplicationList(String uid) {
        List<FriendApplicationDTO> friendApplicationDTOList = friendDao.selectLatestApply(uid,50);
        return friendApplicationDTOList;
    }

    @Override
    public List<FriendGroup> getFriendGroupList(String uid) {
        return friendDao.selectFriendGroupListWithSize(uid);
    }

    @Override
    public Integer addFriendGroup(Integer userId, String uid, String groupName) {
        if(groupName == null){
            throw new BusinessException(ExceptionType.MISSING_PARAMETER_ERROR);
        }
        if(groupName.length() >10)
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"组名长度应小于10");
        Integer affect = friendDao.insertNewFriendGroup(userId, uid,groupName);
        Integer groupId = 0;
        if(affect > 0){
            groupId = friendDao.selectGroupId(uid,groupName);
        }
        return groupId;
    }

    @Override
    public List<UserInfoDTO> getFriendList(String uid) {
        return friendDao.selectFriendList(uid);
    }

    @Override
    public UserInfoDTO getFriendInfo(String uid, String friendId) {
        boolean isNotFriend = friendDao.checkIsFriend(uid,friendId)==0;
        if(isNotFriend){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"这不是你的好友");
        }
        return friendDao.selectFriendInfo(friendId);
    }

    @Override
    public boolean deleteFriend(String uid, String friendId) {
        return friendDao.deleteFriend(uid,friendId)>0;
    }

    @Override
    public boolean updateFriendGroupName(Integer groupId, String groupName,Integer userId) {
        if(StringUtils.isEmpty(groupName)){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"组名不能为空");
        }
        if(groupName.trim().equals("好友")){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名不能与默认组名重复");
        }
        return friendDao.updateFriendGroupName(groupId,groupName,userId)>0;
    }

    @Override
    @Transactional
    public boolean deleteFriendGroup(Integer groupId, String uid) {
        // 1 判断该分组下是否有好友，如果没有直接删除组，结束
        Integer size = friendDao.selectGroupSize(groupId,uid);
        // 2 若有好友，将该分组下所有好友转至默认分组
        boolean moveSuccess = true;
        if(size != 0){
            moveSuccess =  friendDao.moveGroupFriendToDefaultGroup(groupId,uid) > 0;
        }
        // 3 删除掉该分组
        boolean deleteSuccess = friendDao.deleteFriendGroup(groupId, uid)>0;

        return moveSuccess&&deleteSuccess;
    }

    @Override
    @Transactional
    public boolean moveFriendToOtherGroup(String uid, String friendId, Integer oldGroupId, Integer newGroupId) {
        boolean isSuccess = friendDao.moveFriendToAnotherGroup(uid,friendId,oldGroupId,newGroupId)==1;
        return isSuccess;
    }


}
