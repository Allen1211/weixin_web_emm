package com.allen.imsystem.test;

import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.GroupChatMapper;
import com.allen.imsystem.dao.mappers.SearchMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.FriendGroup;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.dto.UserSearchResult;
import com.allen.imsystem.service.IFriendService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext*.xml"})
public class TestMyBatis {

    @Autowired
    SearchMapper searchMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ChatMapper chatMapper;

    @Autowired
    GroupChatMapper groupChatMapper;

    @Autowired
    IFriendService friendService;


//    @Test
//    public void test2() {
//        Map<String, UserSearchResult> map = searchMapper.search("1");
//        List<String> friendId = friendMapper.getAllFriendId("63374315");
//        List<String> requiredId = friendMapper.getAllRequiredToId("63374315");
//        for (String id : friendId) {
//            UserSearchResult result = map.get(id);
//            result.setApplicable(false);
//            result.setReason("已是好友");
//        }
//        for (String id : requiredId) {
//            UserSearchResult result = map.get(id);
//            result.setApplicable(false);
//            result.setReason("已发起申请");
//        }
//        map.get("63374315").setApplicable(false);
//        map.get("63374315").setReason("是自己");
//
//        List<UserSearchResult> resultList = new ArrayList<>(map.values());
//
//    }

//    @Test
//    public void test3() {
//        friendMapper.moveFriendToAnotherGroup("1", "2", 1, 2);
////       friendMapper.moveGroupFriendToDefaultGroup(1,"2");
//    }

//
//    @Test
//    public void test5() {
//        List<FriendGroup> list = friendMapper.selectFriendGroupListWithSize("28661270");
//        System.out.println(list.size());
//    }
////
////    @Test
//    public void test6() {
//        User user = new User("66666666", "111", "123", "222@qq.com");
//        UserInfo userInfo = new UserInfo("66666666","草");
//        userDao.insertUser(user);
//        userDao.insertUserInfo(userInfo);
//
//    }

//    @Test
//    public void test7() {
//        UserInfo userInfo = new UserInfo("66666666","123");
//        userDao.updateUserInfo(userInfo);
//    }
//

    @Test
    public void test8(){
//        List<FriendListByGroupDTO> result = friendService.getFriendListByGroup("28661270");
//        System.out.println(result.size());

    }

//    @Test
//    public void test9(){
//        EditUserInfoDTO result = userDao.selectSelfInfo(25);
//        System.out.println(result.getAvatar());
//    }

    @Test
//    public void test10(){
//        UserInfoDTO userInfoDTO = friendMapper.selectFriendInfo("12345678");
//        System.out.println(userInfoDTO);
//    }
//
//    @Test
//    public void test11(){
//        List<String> uidList = userMapper.selectAllUid();
//        for(String uid : uidList){
//            if(uid.equals("0")) continue;
//            System.out.println(uid);
//            friendMapper.insertNewFriendGroup(uid,"我的好友",true);
//        }
//    }



//    @Test
//    public void testGroupChatMapper(){
//        try {
//            groupChatMapper.selectUnUsedGid();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//
//
//    @Test
    public void testInvite(){
        List<ChatSessionDTO> chatSessionDTOList = chatMapper.selectGroupChatList("28661270");
        System.out.println(chatSessionDTOList);
    }

    @Test
    public void testC(){
        userMapper.insertLoginRecord("123456789",new Date());
    }
}
