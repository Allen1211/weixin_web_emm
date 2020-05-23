package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.EditUserInfoDTO;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户模块相关的业务逻辑接口
 */
@Service
public interface IUserService {

    /**
     * 查询该邮箱地址是否已经被注册
     * @param email
     * @return
     */
    boolean isEmailRegisted(String email);

    User findUserAccountWithUid(String uid);

    UserInfo findUserInfo(String uid);

    /**
     * 根据uid查询用户信息
     */
    UserInfoDTO findUserInfoDTO(String uid);

    /**
     * 多个uid查询多个用户信息
     */
    List<UserInfoDTO> findUserInfoDTOs(List<String> uids);

    /**
     * 用户注册
     * @param email
     * @param password
     * @param username
     */
    void regist(String email, String password, String username);

    /**
     * 用户登录
     * @param uid
     * @param password
     * @return 返回user对象，及newToken
     */
    Map<String,Object> login(String uid, String password);

    /**
     * 用户下线
     * @param uid 用户账号
     */
    void logout(String uid);

    /**
     * 头像上传/更换
     */
    String uploadAvatar(MultipartFile multipartFile, String uid);

    /**
     * 用户信息更改
     */
    boolean updateUserInfo(EditUserInfoDTO editUserInfoDTO, String uid);


    /**
     * 获取自己的信息
     */
    EditUserInfoDTO getSelfInfo(Integer userId);

    /**
     * 获取用户的最后一次的登录时间
     */
    Date getUserLastLoginTime(String uid);

    /**
     * 获取用户在线状态
     */
    Integer getUserOnlineStatus(String uid);

    boolean isOnline(String uid);

    /**
     * 忘记密码。 重设密码
     */
    void forgetPassword(String email, String newPassword);

    /**
     * 修改密码
     */
    String modifyPassword(String uid, String oldPassword, String newPassword);

    void updatePassword(User user, String password);
}
