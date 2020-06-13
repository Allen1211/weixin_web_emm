package com.allen.imsystem.user.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.user.utils.HashSaltUtil;
import com.allen.imsystem.user.utils.JWTUtil;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.user.model.param.EditUserInfoParam;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.model.pojo.User;
import com.allen.imsystem.user.model.pojo.UserInfo;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.friend.service.FriendGroupService;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.service.UserService;
import com.allen.imsystem.id.IdPoolService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.allen.imsystem.common.Const.GlobalConst.*;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendGroupService friendGroupService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private FileService fileService;

    @Autowired
    private IdPoolService idPoolService;

    @Override
    public User findUserAccountWithUid(String uid) {
        return userMapper.selectUserWithUid(uid);
    }

    @Override
    public UserInfo findUserInfo(String uid) {
        return userMapper.selectUserInfo(uid);
    }


    public UserInfoView findUserInfoDTO(String uid){
        UserInfoView userInfoView = (UserInfoView) redisService.get(RedisKey.KEY_USER_INFO + uid);
        if(userInfoView == null){
            userInfoView = userMapper.selectUserInfoDTO(uid);
            redisService.set(RedisKey.KEY_USER_INFO + uid, userInfoView, 15L, TimeUnit.MINUTES);
        }
        return userInfoView;
    }

    public List<UserInfoView> findUserInfoDTOs(List<String> uids){
        List<UserInfoView> userInfoViews = new ArrayList<>(uids.size());
        for(String uid : uids){
            userInfoViews.add(this.findUserInfoDTO(uid));
        }
        return userInfoViews;
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
        /*
         * 从uid池里获取一个未使用的uid
         */
        String uid = idPoolService.nextId(IdType.UID);
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
        userMapper.insertLoginRecord(uid, new Date());
        // 为新用户创建一个默认分组
        friendGroupService.addFriendGroup(uid, "我的好友", true);
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
        redisService.hset(RedisKey.KEY_USER_STATUS, user.getUid(), GlobalConst.UserStatus.ONLINE);

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
        redisService.hset(RedisKey.KEY_USER_STATUS, uid, GlobalConst.UserStatus.OFFLINE);

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
    public boolean updateUserInfo(EditUserInfoParam editUserInfoParam, String uid) {
        if (StringUtils.isEmpty(editUserInfoParam.getUsername()) || editUserInfoParam.getUsername().length() > 10) {
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"用户名长度不应超过十个字符");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setUpdateTime(new Date());
        userInfo.setDesc(editUserInfoParam.getSignWord());
        userInfo.setIconId(editUserInfoParam.getAvatar());
        userInfo.setGender(editUserInfoParam.getGender());
        userInfo.setUsername(editUserInfoParam.getUsername());

        return userMapper.updateUserInfo(userInfo) > 0;
    }

    @Override
    public EditUserInfoParam getSelfInfo(Integer userId) {
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
