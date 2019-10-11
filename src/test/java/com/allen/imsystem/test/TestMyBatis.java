package com.allen.imsystem.test;

import com.allen.imsystem.dao.FriendDao;
import com.allen.imsystem.dao.SearchDao;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.model.dto.UserSearchResult;
import com.allen.imsystem.model.pojo.UidPool;
import com.allen.imsystem.model.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestMyBatis {

    @Autowired
    UserDao userDao;

    @Autowired
    SearchDao searchDao;

    @Autowired
    FriendDao friendDao;

    @Test
    public void test1(){
//        Assert.assertEquals(0,userDao.countUid("12345").intValue());
        User user = userDao.findUserWithEmail("234@23.com");
        System.out.println(user.getTel());
    }

    @Test
    public void test2(){
        Map<String, UserSearchResult> map = searchDao.searchUserByKeyword("1");
        List<String> friendId = friendDao.getAllFriendId("63374315");
        List<String> requiredId = friendDao.getAllRequiredToId("63374315");
        for(String id:friendId){
            UserSearchResult result = map.get(id);
            result.setApplicable(false);
            result.setReason("已是好友");
        }
        for(String id:requiredId){
            UserSearchResult result = map.get(id);
            result.setApplicable(false);
            result.setReason("已发起申请");
        }
        map.get("63374315").setApplicable(false);
        map.get("63374315").setReason("是自己");

        List<UserSearchResult> resultList = new ArrayList<>(map.values());

    }

    @Test
    public void test3(){
       friendDao.moveFriendToAnotherGroup("1","2",1,2);
//       friendDao.moveGroupFriendToDefaultGroup(1,"2");
    }

    @Test
    public void test4(){
       UidPool uidPool = userDao.selectNextUnUsedUid();
       System.out.println(uidPool.getId() + "   " +uidPool.getUid());
       Integer affected = userDao.sortDeleteUsedUid(uidPool.getId());
       System.out.println(affected);
//       friendDao.moveGroupFriendToDefaultGroup(1,"2");
    }
}
