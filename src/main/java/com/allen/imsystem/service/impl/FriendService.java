package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.dao.ChatDao;
import com.allen.imsystem.dao.FriendDao;
import com.allen.imsystem.dao.SearchDao;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.FriendRelation;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IFriendService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FriendService implements IFriendService {

    @Autowired
    private SearchDao searchDao;

    @Autowired
    private FriendDao friendDao;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private IChatService chatService;

    @Override
    public List<UserSearchResult> searchStranger(String uid, String keyword) {
        Map<String, UserSearchResult> map = searchDao.searchUserByKeyword(keyword);
        if (map == null)
            new ArrayList<UserSearchResult>();
        List<String> friendId = friendDao.getAllFriendId(uid);
        List<String> requiredId = friendDao.getAllRequiredToId(uid);
        for (String id : friendId) {
            UserSearchResult result = map.get(id);
            if (result != null) {
                result.setApplicable(false);
                result.setReason("已添加");
                map.put(id, result);
            }
        }
        for (String id : requiredId) {
            UserSearchResult result = map.get(id);
            if (result != null) {
                result.setApplicable(false);
                result.setReason("已申请");
                map.put(id, result);
            }
        }
        UserSearchResult result = map.get(uid);
        if (result != null) {
            result.setApplicable(false);
            result.setReason("是自己");
            map.put(uid, result);
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public Boolean checkIsMyFriend(String uid, String friendId) {
        FriendRelation friendRelation = friendDao.selectFriendRelation(uid, friendId);
        if (friendRelation == null) {
            return false;
        }
        if (friendRelation.getUidA().equals(uid)) {
            boolean deleteIt = friendRelation.getADeleteB();
            return !deleteIt;
        } else {
            boolean deleteIt = friendRelation.getBDeleteA();
            return !deleteIt;
        }
    }

    public Boolean checkIsDeletedByFriend(String uid,String friendId){
        FriendRelation friendRelation = friendDao.selectFriendRelation(uid, friendId);
        if (friendRelation == null) {
            return true;
        }
        if (friendRelation.getUidA().equals(uid)) {
            return friendRelation.getBDeleteA();
        } else {
            return friendRelation.getADeleteB();
        }
    }

    @Override
    @Transactional
    public boolean addFriendApply(ApplyAddFriendDTO params, String uid) {
        String fromUId = uid;
        String toUId = params.getFriendId();
        String reason = params.getApplicationReason();
        if (reason == null) reason = "";

        Integer groupId = null;

        if(params.getGroupId() == null){
            FriendGroupPojo defaultGroup = friendDao.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        }else{
            groupId = Integer.valueOf(params.getGroupId());
        }
        return friendDao.addFriendApply(fromUId, toUId, groupId, reason) > 0;
    }

    @Override
    @Transactional
    public boolean passFriendApply(String uid, String friendId, Integer groupId) {
        // 1 查询对方要把ta放到什么组
        Integer bePutInGroupId = friendDao.selectApplyGroupId(friendId, uid);
        // 如果没有设定组，则默认放入默认组
        if (groupId == null) {
            FriendGroupPojo defaultGroup = friendDao.selectUserDefaultFriendGroup(uid);
            groupId = defaultGroup.getGroupId();
        }
        // 2 更新用户申请表，将对方对当前用户的申请通过，同时也把当前用户对对方的申请全部通过
        boolean successUpdate = friendDao.updateFriendApplyPass(true, friendId, uid) > 0
                || friendDao.updateFriendApplyPass(true, uid, friendId) > 0;
        if (!successUpdate) { // 如果更新行数为0，说明不存在此申请或者申请已经被同意
            throw new BusinessException(ExceptionType.APPLY_HAS_BEEN_HANDLER);
        }

        // 3.5 判定对方是否已经是自己的好友，如果是，删掉原来的关系，再执行插入关系。若不是，直接执行插入
        boolean isMyFriend = checkIsMyFriend(uid,friendId);
        if(isMyFriend){
            // 删掉原有的好友关系
            friendDao.deleteFriend(uid, friendId);
            // 删除原有的会话
            String uidA = uid.compareTo(friendId)<0? uid:friendId;
            String uidB = uid.compareTo(friendId)<0? friendId:uid;
            chatMapper.hardDeletePrivateChat(uidA,uidB);
        }

        // 4 插入好友表， 防止重复，限定uid小的作为uid_a,uid大的作为uid_b
        boolean successInsert = false;
        if (uid.compareTo(friendId) < 0) {
            successInsert = friendDao.insertNewFriend(uid, friendId, bePutInGroupId, groupId) > 0;
        } else if (uid.compareTo(friendId) > 0) {
            successInsert = friendDao.insertNewFriend(friendId, uid, groupId, bePutInGroupId) > 0;
        } else {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "不能添加自己为好友");
        }

        // 4 为该对好友新建个会话
        new Thread(() -> {
            chatService.initNewPrivateChat(uid, friendId, false);
        }).start();

        return successInsert && successUpdate;
    }

    @Override
    public List<FriendApplicationDTO> getFriendApplicationList(String uid) {
        List<FriendApplicationDTO> friendApplicationDTOList = friendDao.selectLatestApply(uid, 50);
        return friendApplicationDTOList;
    }

    @Override
    public List<FriendGroup> getFriendGroupList(String uid) {
        return friendDao.selectFriendGroupListWithSize(uid);
    }

    @Override
    public List<FriendListByGroupDTO> getFriendListByGroup(String uid) {
        // 按组id升序排列的 好友列表
        List<UserInfoDTO> friendListOrderByGroup = friendDao.selectFriendListOrderByGroupId(uid);
        // 按组id升序排列的 分组列表
        List<FriendGroup> friendGroupList = friendDao.selectFriendGroupListWithSize(uid);
        // 按组id升序排列的 分组好友列表
        List<FriendListByGroupDTO> resultList = new ArrayList<>(friendGroupList.size());
        int begin = 0;
        for (FriendGroup friendGroup : friendGroupList) {
            FriendListByGroupDTO dto = new FriendListByGroupDTO();
            dto.setGroupId(friendGroup.getGroupId());
            dto.setGroupName(friendGroup.getGroupName());
            dto.setGroupSize(friendGroup.getGroupSize());
            dto.setIsDefault(friendGroup.getIsDefault());
            // 根据每一个组的大小
            int groupSize = friendGroup.getGroupSize();

            if (groupSize != 0) {
                dto.setMembers(friendListOrderByGroup.subList(begin, begin + friendGroup.getGroupSize()));
                begin += friendGroup.getGroupSize();
            } else {
                dto.setMembers(new ArrayList<>());
            }
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    public Integer addFriendGroup(String uid, String groupName,Boolean isDefault) {
        if (groupName == null) {
            throw new BusinessException(ExceptionType.MISSING_PARAMETER_ERROR);
        }
        if (groupName.length() > 10)
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名长度应小于10");
        Integer affect = friendDao.insertNewFriendGroup(uid, groupName,isDefault);
        Integer groupId = 0;
        if (affect > 0) {
            groupId = friendDao.selectGroupId(uid, groupName);
        }
        return groupId;
    }

    @Override
    public Set<UserInfoDTO> getFriendList(String uid) {
        return friendDao.selectFriendList(uid);
    }

    @Override
    public UserInfoDTO getFriendInfo(String uid, String friendId) {
        boolean isNotFriend = friendDao.checkIsFriend(uid, friendId) == 0;
        if (isNotFriend) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "这不是你的好友");
        }
        return friendDao.selectFriendInfo(friendId);
    }

    @Override
    @Transactional
    public boolean deleteFriend(String uid, String friendId) {
        // 1、移除掉与好友的会话
        chatService.removePrivateChat(uid,friendId);
        // 2、检查是否已经被好友删除
        Boolean isDeletedByFriend = checkIsDeletedByFriend(uid,friendId);
        if (isDeletedByFriend)//如果已经被对方删除，则执行物理删除
        {
            return friendDao.deleteFriend(uid, friendId) > 0;
        }
        else {   // 否则执行逻辑删除
            return sortDeleteFriend(uid,friendId);
        }
    }

    private boolean sortDeleteFriend(String uid,String friendId){
        if (uid.compareTo(friendId) < 0) {
            return friendDao.sortDeleteFriendA2B(uid, friendId) > 0;
        } else {
            return friendDao.sortDeleteFriendB2A(uid, friendId) > 0;
        }
    }

    @Override
    public boolean updateFriendGroupName(Integer groupId, String groupName, String uid) {
        if (StringUtils.isEmpty(groupName)) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名不能为空");
        }
        return friendDao.updateFriendGroupName(groupId, groupName, uid) > 0;
    }

    @Override
    @Transactional
    public boolean deleteFriendGroup(Integer groupId, String uid) {
        // 1 判断该分组下是否有好友，如果没有直接删除组，结束
        Integer size = friendDao.selectGroupSize(groupId, uid);
        // 2 获取该用户的默认分组，若删除的是默认分组，报错。
        FriendGroupPojo defaultGroup = friendDao.selectUserDefaultFriendGroup(uid);
        if(groupId.equals(defaultGroup.getGroupId())){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"不能删除默认分组");
        }
        boolean moveSuccess = true;
        if (size > 0) {// 若有好友，将该分组下所有好友转至默认分组
            Integer affect = friendDao.moveGroupFriendToDefaultGroup(defaultGroup.getGroupId(),groupId, uid);
            System.out.println(affect);
            moveSuccess = friendDao.moveGroupFriendToDefaultGroup(defaultGroup.getGroupId(),groupId, uid) > 0;
        }
        // 3 删除掉该分组
        boolean deleteSuccess = friendDao.deleteFriendGroup(groupId, uid) > 0;

        return moveSuccess && deleteSuccess;
    }

    @Override
    @Transactional
    public boolean moveFriendToOtherGroup(String uid, String friendId, Integer oldGroupId, Integer newGroupId) {
        Boolean isGroupValid = friendDao.isGroupValid(newGroupId);
        if (isGroupValid == null || isGroupValid.booleanValue() == false) {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "要移动到的组不存在或已被删除");
        }
        boolean isSuccess = friendDao.moveFriendToAnotherGroup(uid, friendId, oldGroupId, newGroupId) > 0;
        return isSuccess;
    }


}
