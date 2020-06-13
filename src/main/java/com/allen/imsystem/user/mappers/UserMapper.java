package com.allen.imsystem.user.mappers;

import com.allen.imsystem.user.model.param.EditUserInfoParam;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.model.pojo.User;
import com.allen.imsystem.user.model.pojo.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Mapper
public interface UserMapper {

    User selectUserWithUid(String uid);

    User selectUserWithEmail(String email);

    /**
     * 根据uid查询用户信息
     */
    UserInfoView selectUserInfoDTO(String uid);

    /**
     * 根据uid列表查询多个用户信息
     */
    List<UserInfoView> selectUserInfoDTOsByUids(@Param("uids") List<String> uids);

    UserInfo selectUserInfo(String uid);

    Integer selectCountUid(String uid);

    List<String> selectAllUid();

    Integer selectCountEmail(String email);

    EditUserInfoParam selectSelfInfo(Integer userId);

    Date getUserLastLoginTime(String uid);

    Integer insertUser(User user);

    Integer insertUserInfo(UserInfo userInfo);

    Integer updateUserByEmail(User user);

    Integer updateUserInfo(UserInfo userInfo);

    Integer updateLoginRecord(@Param("uid") String uid, @Param("loginTime") Date loginTime);

    Integer insertLoginRecord(@Param("uid") String uid, @Param("loginTime") Date loginTime);
}
