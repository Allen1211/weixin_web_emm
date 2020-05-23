package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.HashSaltUtil;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.mappers.UserMapper;
import com.allen.imsystem.model.dto.EditUserInfoDTO;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.pojo.UidPool;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.IFriendGroupService;
import com.allen.imsystem.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements IUserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IFriendGroupService friendGroupService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IFileService fileService;

    @Override
    public User findUserAccountWithUid(String uid) {
        return userMapper.selectUserWithUid(uid);
    }

    @Override
    public UserInfo findUserInfo(String uid) {
        return userMapper.selectUserInfo(uid);
    }

    public UserInfoDTO findUserInfoDTO(String uid){
        UserInfoDTO userInfoDTO = (UserInfoDTO) redisService.get(GlobalConst.Redis.KEY_USER_INFO + uid);
        if(userInfoDTO == null){
            userInfoDTO = userMapper.selectUserInfoDTO(uid);
            redisService.set(GlobalConst.Redis.KEY_USER_INFO + uid, userInfoDTO, 15L, TimeUnit.MINUTES);
        }
        return userInfoDTO;
    }

    public List<UserInfoDTO> findUserInfoDTOs(List<String> uids){
        List<UserInfoDTO> userInfoDTOs = new ArrayList<>(uids.size());
        for(String uid : uids){
            userInfoDTOs.add(this.findUserInfoDTO(uid));
        }
        return userInfoDTOs;
    }

    @Override
    public boolean isEmailRegisted(String email) {
        if (StringUtils.isEmpty(email) || !email.matches(GlobalConst.RegExp.EMAIL)) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "邮箱格式不合法");
        }
        return userMapper.selectCountEmail(email) > 0;
    }

    @Override
    @Transactional
    public void regist(String email, String password, String username) {

        /**
         * 从uid池里获取一个未使用的uid
         */
        UidPool uidPool = userMapper.selectNextUnUsedUid();
        String uid = uidPool.getUid();
        //对密码加密
        String salt = UUID.randomUUID().toString();
        password = HashSaltUtil.getHashSaltPwd(password, salt);

        // 插入数据库
        User user = new User(uid, password, salt, email);
        UserInfo userInfo = new UserInfo(uid, username);
        Random random = new Random(System.currentTimeMillis());
        int defaultIconId = random.nextInt(13) + 1;
        userInfo.setIconId("default/" + defaultIconId + ".jpg");
        userMapper.insertUser(user);
        userMapper.insertUserInfo(userInfo);
        userMapper.sortDeleteUsedUid(uidPool.getId());
        userMapper.insertLoginRecord(uid, new Date());
        // 为新用户创建一个默认分组
        friendGroupService.addFriendGroup(uid, "我的好友", true);
        return;
    }

    /**
     * 账号密码登录， 成功返回token，失败抛出业务异常
     *
     * @param password
     * @return
     */
    @Override
    public Map<String, Object> login(String uidOrEmail, String password) {
        User user = null;
        // 判断是email还是uid
        if (uidOrEmail.contains("@")) {
            user = userMapper.selectUserWithEmail(uidOrEmail);
        } else {
            user = userMapper.selectUserWithUid(uidOrEmail);
        }

        if (null == user)
            throw new BusinessException(ExceptionType.USERNAME_PASSWORD_ERROR);
        String hashPassword = user.getPassword();
        String salt = user.getSalt();
        if (!hashPassword.equals(HashSaltUtil.getHashSaltPwd(password, salt)))
            throw new BusinessException(ExceptionType.USERNAME_PASSWORD_ERROR);

        // 更新最后一次登录时间
        userMapper.updateLoginRecord(user.getUid(), new Date());
        // redis更新该用户的在线状态 至在线
        redisService.hset(GlobalConst.Redis.KEY_USER_STATUS, user.getUid(), GlobalConst.UserStatus.ONLINE);

        /**
         * 颁发token
         */
        String newToken = JWTUtil.createLoginToken(user.getUid(), user.getId());
        Map<String, Object> map = new HashMap<>(2);
        map.put("newToken", newToken);
        map.put("user", user);
        return map;
    }


    @Override
    public void logout(String uid) {
        User user = userMapper.selectUserWithUid(uid);
        if (user == null) {
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        redisService.hset(GlobalConst.Redis.KEY_USER_STATUS, uid, GlobalConst.UserStatus.OFFLINE);

    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        String avatarURL = fileService.uploadAvatar(multipartFile, uid);
        if (avatarURL != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(uid);
            userInfo.setIconId(avatarURL);
            userMapper.updateUserInfo(userInfo);
        } else {
            throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        return GlobalConst.Path.AVATAR_URL + avatarURL;
    }

    @Override
    public boolean updateUserInfo(EditUserInfoDTO editUserInfoDTO, String uid) {
        if (StringUtils.isEmpty(editUserInfoDTO.getUsername()) || editUserInfoDTO.getUsername().length() > 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"用户名长度不应超过十个字符");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setUpdateTime(new Date());
        userInfo.setDesc(editUserInfoDTO.getSignWord());
        userInfo.setIconId(editUserInfoDTO.getAvatar());
        userInfo.setGender(editUserInfoDTO.getGender());
        userInfo.setUsername(editUserInfoDTO.getUsername());

        return userMapper.updateUserInfo(userInfo) > 0;
    }

    @Override
    public EditUserInfoDTO getSelfInfo(Integer userId) {
        if (userId == null) {
            throw new BusinessException(ExceptionType.NO_LOGIN_ERROR);
        }
        return userMapper.selectSelfInfo(userId);
    }

    @Override
    public Date getUserLastLoginTime(String uid) {
        return userMapper.getUserLastLoginTime(uid);
    }

    @Override
    public Integer getUserOnlineStatus(String uid) {
        Integer onlineStatus = (Integer) redisService.hget("user_status", uid);
        if (onlineStatus == null) onlineStatus = 1;
        return onlineStatus;
    }

    @Override
    public boolean isOnline(String uid) {
        if (uid == null) return false;
        Integer onlineStatus = getUserOnlineStatus(uid);
        return !onlineStatus.equals(0);
    }

    @Override
    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectUserWithEmail(email);
        if (user == null) {
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        // 密码检验并更新
        updatePassword(user, newPassword);
    }

    @Override
    public String modifyPassword(String uid, String oldPassword, String newPassword) {
        User user = userMapper.selectUserWithUid(uid);
        if (user == null) {
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        if (!user.getPassword().equals(HashSaltUtil.getHashSaltPwd(oldPassword, user.getSalt()))) {
            throw new BusinessException(ExceptionType.PERMISSION_DENIED, "旧密码错误");
        }
        // 密码检验并更新
        updatePassword(user, newPassword);
        // 生成新token
        String token = JWTUtil.createLoginToken(user.getUid(), user.getId());
        return token;
    }


    public void updatePassword(User user, String newPassword) {
        // 密码检验
        if (StringUtils.isEmpty(newPassword.trim()) || newPassword.length() < 6 || newPassword.length() > 12) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "新密码长度应在6-12之间");
        }
        String salt = UUID.randomUUID().toString();
        newPassword = HashSaltUtil.getHashSaltPwd(newPassword, salt);
        user.setPassword(newPassword);
        user.setSalt(salt);
        user.setUpdateTime(new Date());
        userMapper.updateUserByEmail(user);
    }
}
