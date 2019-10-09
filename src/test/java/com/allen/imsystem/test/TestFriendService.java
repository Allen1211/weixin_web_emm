package com.allen.imsystem.test;

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
        boolean success = friendService.passFriendApply("63374315","97554417",1);
        System.out.println(success);
    }
}
