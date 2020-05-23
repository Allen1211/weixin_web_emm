package com.allen.imsystem.test.friend;

import com.allen.imsystem.mappers.FriendGroupMapper;
import com.allen.imsystem.mappers.FriendMapper;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.FriendRelation;
import com.allen.imsystem.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName TestFriendGroup
 * @Description
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
public class TestFriendGroup extends BaseTest {

    @Autowired
    FriendGroupMapper friendGroupMapper;

    @Autowired
    FriendMapper friendMapper;

    @Test
    public void testUpdateFriendGroupSize(){
        List<FriendGroupPojo> allGroups = friendGroupMapper.selectAll();
        for(FriendGroupPojo group : allGroups){
            int size = friendGroupMapper.selectGroupSize(group.getGroupId(), group.getUid());
            group.setSize(size);
            System.out.println(size);
            friendGroupMapper.updateById(group);
        }
    }

    @Test
    public void testReplaceDefaultGroup(){
        List<FriendRelation> friendRelations = friendMapper.selectAll();
        for(FriendRelation friendRelation: friendRelations){
            boolean isChange = false;
            if(friendRelation.getAInbGroupId() == 1){
                FriendGroupPojo defaultGroup = friendGroupMapper.selectUserDefaultFriendGroup(friendRelation.getUidB());
                friendRelation.setAInbGroupId(defaultGroup.getGroupId());
                isChange = true;
            }
            if(friendRelation.getBInaGroupId() == 1){
                FriendGroupPojo defaultGroup = friendGroupMapper.selectUserDefaultFriendGroup(friendRelation.getUidA());
                friendRelation.setBInaGroupId(defaultGroup.getGroupId());
                isChange = true;
            }
            if(isChange){
                friendMapper.updateById(friendRelation);
            }
        }
    }
}
