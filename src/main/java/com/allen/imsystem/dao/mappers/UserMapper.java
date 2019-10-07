package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;

public interface UserMapper {
    User selectUserWithUid(String uid);

    User selectUserWithEmail(String email);

    UserInfo selectUserInfo(String uid);

    Integer selectCountUid(String uid);

    Integer selectCountEmail(String email);

    Integer insertUser(User user);

    Integer insertUserInfo(UserInfo userInfo);

    Integer updateUser(User user);

    Integer updateUserInfo(UserInfo userInfo);


}
