package com.allen.imsystem.test;

import com.allen.imsystem.dao.FriendDao;
import com.allen.imsystem.dao.mappers.FriendMapper;
import com.allen.imsystem.model.dto.ApplyAddFriendDTO;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import com.allen.imsystem.model.pojo.FriendRelation;
import com.allen.imsystem.service.IFriendService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestFriendService {

    @Autowired
    private IFriendService friendService;

    @Autowired
    private FriendDao friendDao;

    @Autowired
    private FriendMapper friendMapper;

    @Test
    public void test1(){
        List<FriendApplicationDTO> list = friendService.getFriendApplicationList("63374315");
        System.out.println(list.size());
    }
//    @Test
//    public void test2(){
//        ApplyAddFriendDTO params = new ApplyAddFriendDTO();
//        params.setApplicationReason("测试");
//        params.setUid("63374315");
//        params.setFriendId("97554417");
//        params.setGroupId("1");
//        boolean change = friendService.addFriendApply(params);
//        System.out.println(change);
//    }
    @Test
    public void test3(){
        boolean success = friendService.passFriendApply("10547348","23456789",1);
        success = friendService.passFriendApply("63374315","10547348",1);
        success = friendService.passFriendApply("81309655","10547348",1);
//        System.out.println(success);
    }

    @Test
    public void test4(){
        Integer a = friendDao.selectApplyGroupId("28661270","10547348");;
//        boolean success = friendService.passFriendApply("10547348","28661270",1);
//        System.out.println(success);
        System.out.println(a);
    }

    @Test
    public void Test5(){
        boolean is = friendService.checkIsDeletedByFriend("81309655","28661270");
        System.out.println(is);
    }

    @Test
    public void updateGroup(){
        List<FriendRelation> friendRelationList = friendMapper.selectAllFriendRelation();
        for(FriendRelation fr:friendRelationList){
            String uidA = fr.getUidA();
            String uidB = fr.getUidB();
            Integer aInB = fr.getAInbGroupId();
            Integer bInA = fr.getBInaGroupId();

            if(aInB.equals(1)){
                FriendGroupPojo defaultGroup = friendDao.selectUserDefaultFriendGroup(uidB);
                Integer bDefaultGroupId = defaultGroup.getGroupId();
                fr.setAInbGroupId(bDefaultGroupId);
            }

            if(bInA.equals(1)){
                FriendGroupPojo defaultGroup = friendDao.selectUserDefaultFriendGroup(uidA);
                Integer aDefaultGroupId = defaultGroup.getGroupId();
                fr.setBInaGroupId(aDefaultGroupId);
            }
            friendMapper.updateFriendRelation(fr);
        }
    }
}
