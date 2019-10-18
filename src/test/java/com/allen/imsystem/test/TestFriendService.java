package com.allen.imsystem.test;

import com.allen.imsystem.dao.FriendDao;
import com.allen.imsystem.model.dto.ApplyAddFriendDTO;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
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
        System.out.println(friendDao.selectApplyGroupId("28661270","10547348"));;
        boolean success = friendService.passFriendApply("10547348","28661270",1);
//        System.out.println(success);
    }

    @Test
    public void test4(){
        Integer a = friendDao.selectApplyGroupId("28661270","10547348");;
//        boolean success = friendService.passFriendApply("10547348","28661270",1);
//        System.out.println(success);
        System.out.println(a);
    }

}
