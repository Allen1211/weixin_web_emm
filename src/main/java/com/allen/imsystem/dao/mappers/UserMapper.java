package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.EditUserInfoDTO;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.pojo.UidPool;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

public interface UserMapper {


    User selectUserWithUid(String uid);

    User selectUserWithEmail(String email);

    UserInfoDTO selectSenderInfo(String uid);

    UserInfo selectUserInfo(String uid);

    Integer selectCountUid(String uid);

    List<String> selectAllUid();

    Integer selectCountEmail(String email);

    UidPool selectNextUnUsedUid();

    EditUserInfoDTO selectSelfInfo(Integer userId);

    Date getUserLastLoginTime(String uid);

    Integer insertUser(User user);

    Integer insertUserInfo(UserInfo userInfo);

    Integer updateUserByEmail(User user);

    Integer updateUserInfo(UserInfo userInfo);

    Integer insertBatchIntoUidPool(List<String> list);

    Integer insertBatchIntoGidPool(List<String> list);

    Integer sortDeleteUsedUid(Integer id);

    Integer updateLoginRecord(@Param("uid") String uid,@Param("loginTime")Date loginTime);

    Integer insertLoginRecord(@Param("uid") String uid,@Param("loginTime")Date loginTime);
}
