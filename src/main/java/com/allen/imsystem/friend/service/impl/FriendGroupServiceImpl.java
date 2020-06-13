package com.allen.imsystem.friend.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.friend.mappers.FriendGroupMapper;
import com.allen.imsystem.friend.mappers.FriendMapper;
import com.allen.imsystem.friend.model.vo.FriendGroupView;
import com.allen.imsystem.friend.model.pojo.FriendGroupPojo;
import com.allen.imsystem.friend.model.pojo.FriendRelation;
import com.allen.imsystem.friend.service.FriendGroupService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName FriendGroupService
 * @Description 好友分组相关业务逻辑实现
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
@Service
public class FriendGroupServiceImpl implements FriendGroupService {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendGroupMapper friendGroupMapper;

    /**
     * 获取用户的好友分组列表
     * @param uid 用户uid
     */
    @Override
    public List<FriendGroupView> getFriendGroupList(String uid) {
        return friendGroupMapper.selectFriendGroupListWithSize(uid);

    }

    /**
     * 新建一个好友分组
     * @param uid 用户uid
     * @param groupName 分组名
     * @param isDefault 是否默认分组
     */
    @Override
    public Integer addFriendGroup(String uid, String groupName, Boolean isDefault) {
        if (groupName == null) {
            throw new BusinessException(ExceptionType.MISSING_PARAMETER_ERROR);
        }
        if (groupName.length() > 10)
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名长度应小于10");
        boolean groupNameHasExist = friendGroupMapper.selectGroupId(uid,groupName) !=null ;
        if(groupNameHasExist){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"组名重复");
        }
        FriendGroupPojo friendGroupPojo = new FriendGroupPojo(groupName, uid, 0, true, isDefault);
        int affect = friendGroupMapper.insert(friendGroupPojo);
        if(affect <= 0){
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
        return friendGroupPojo.getGroupId();
    }

    /**
     * 更改好友分组组名
     *
     * @param groupId 分组id
     * @param groupName 分组名
     * @param uid 用户uid
     */
    @Override
    public boolean updateFriendGroupName(Integer groupId, String groupName, String uid) {
        FriendGroupPojo friendGroup = friendGroupMapper.selectById(groupId);
        if(Objects.isNull(friendGroup)){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "分组不存在");
        }
        if (StringUtils.isEmpty(groupName) ) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "组名不能为空");
        }
        if (StringUtils.length(groupName) > 10){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"好友分组名长度不得超过10个字符");
        }
        friendGroup.setFriendGroupName(groupName);
        return friendGroupMapper.updateById(friendGroup) > 0;
    }

    /**
     * 删除一个好友分组，内部如果有好友则全移到默认分组
     *
     * @param groupId 分组id
     * @param uid 用户uid
     */
    @Override
    @Transactional
    public boolean deleteFriendGroup(Integer groupId, String uid) {
        FriendGroupPojo friendGroup = friendGroupMapper.selectById(groupId);
        if(Objects.isNull(friendGroup)){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "好友分组不存在");
        }
        // 获取该用户的默认分组，若删除的是默认分组，报错。
        if (friendGroup.getIsDefault()) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "不能删除默认分组");
        }
        // 判断该分组下是否有好友，如果没有直接删除组，结束
        Integer size = friendGroup.getSize();
        if (size > 0) {// 若有好友，将该分组下所有好友转至默认分组
            FriendGroupPojo defaultGroup = friendGroupMapper.selectUserDefaultFriendGroup(uid);
            boolean moveSuccess = friendGroupMapper.moveGroupFriendToDefaultGroup(defaultGroup.getGroupId(), groupId, uid) > 0;
            if(moveSuccess){
                // 更新默认组的人数
                defaultGroup.setSize(defaultGroup.getSize() + size);    // 默认组新人数 = 默认组人数 + 被删除组的人数
                friendGroupMapper.updateById(defaultGroup);
            }else{
                throw new BusinessException(ExceptionType.SERVER_ERROR, "删除失败，无法将原组好友转到默认组");
            }
        }
        // 3 删除掉该分组
        return friendGroupMapper.deleteFriendGroup(groupId, uid) > 0;
    }

    /**
     * 移动好友到另一个分组
     *
     * @param uid 用户uid
     * @param friendId 好友uid
     * @param oldGroupId 旧分组id
     * @param newGroupId 新分组id
     */
    @Override
    @Transactional
    public boolean moveFriendToOtherGroup(String uid, String friendId, Integer oldGroupId, Integer newGroupId) {
        FriendGroupPojo oldFriendGroup = friendGroupMapper.selectById(oldGroupId);
        FriendGroupPojo newFriendGroup = friendGroupMapper.selectById(newGroupId);
        if( Objects.isNull(oldFriendGroup)){
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "原分组不存在或已被删除");
        }
        if (Objects.isNull(newFriendGroup)) {
            throw new BusinessException(ExceptionType.DATA_CONSTRAINT_FAIL, "要移动到的组不存在或已被删除");
        }
        // 源分组人数-1，目的分组人数+1
        oldFriendGroup.setSize(oldFriendGroup.getSize() <= 0 ? 0 : oldFriendGroup.getSize() - 1);
        newFriendGroup.setSize(newFriendGroup.getSize() + 1);
        // 更新分组人数
        friendGroupMapper.updateById(oldFriendGroup);
        friendGroupMapper.updateById(newFriendGroup);

        // 更新好友关系中的分组信息
        FriendRelation friendRelation = friendMapper.selectFriendRelation(uid, friendId);
        if(Objects.isNull(friendRelation)){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "好友关系不存在");
        }
        if(friendRelation.getUidA().equals(uid)){
            friendRelation.setBInaGroupId(newGroupId);
        }else{
            friendRelation.setAInbGroupId(newGroupId);
        }
        friendRelation.setUpdateTime(new Date());
        return friendMapper.updateById(friendRelation) > 0;
    }
}
