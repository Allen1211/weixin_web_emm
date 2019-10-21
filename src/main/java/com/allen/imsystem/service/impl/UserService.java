package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.ParamValidator;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.model.dto.EditUserInfoDTO;
import com.allen.imsystem.model.pojo.UidPool;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.IUserService;
import com.allen.imsystem.common.utils.HashSaltUtil;
import com.allen.imsystem.common.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class UserService implements IUserService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IFileService fileService;

    @Override
    public User findUserAccountWithUid(String uid) {
        return userDao.findUserWithUid(uid);
    }

    @Override
    public UserInfo findUserInfo(String uid) {
        return userDao.findUserInfo(uid);
    }

    @Override
    public boolean isEmailRegisted(String email){
        ParamValidator validator = new ParamValidator();
        validator.validateEmail(email, "邮箱格式不合法");
        validator.doValidate();
        return userDao.countEmail(email) > 0;
    }

    @Override
    @Transactional
    public void regist(String email, String password, String username){

        /**
         * 从uid池里获取一个未使用的uid
         */
        UidPool uidPool = userDao.selectNextUnUsedUid();
        String uid = uidPool.getUid();
        //对密码加密
        String salt = UUID.randomUUID().toString();
        password = HashSaltUtil.getHashSaltPwd(password,salt);

        // 插入数据库
        User user = new User(uid,password,salt,email);
        UserInfo userInfo = new UserInfo(uid,username);
        Random random = new Random(System.currentTimeMillis());
        int defaultIconId = random.nextInt(13) + 1;
        userInfo.setIconId(GlobalConst.Path.AVATAR_URL+"default/"+defaultIconId+".jpg");
        userDao.insertUser(user);
        userDao.insertUserInfo(userInfo);
        userDao.sortDeleteUsedUid(uidPool.getId());
        userDao.insertLoginRecord(uid,new Date());
        return;
    }

    /**
     * 账号密码登录， 成功返回token，失败抛出业务异常
     * @param password
     * @return
     */
    @Override
    public Map<String,Object> login(String uidOrEmail, String password){
        User user = null;
        // 判断是email还是uid
        if(uidOrEmail.contains("@")){
            user = userDao.findUserWithEmail(uidOrEmail);
        }else{
            user = userDao.findUserWithUid(uidOrEmail);
        }

        if(null == user)
            throw new BusinessException(ExceptionType.USERNAME_PASSWORD_ERROR);
        String hashPassword = user.getPassword();
        String salt = user.getSalt();
//        if(!hashPassword.equals(HashSaltUtil.getHashSaltPwd(password,salt)))
//            throw new BusinessException(ExceptionType.USERNAME_PASSWORD_ERROR);

        // 更新最后一次登录时间
        userDao.updateLoginRecord(user.getUid(),new Date());
        // redis更新该用户的在线状态 至在线
        redisService.hset("user_status",user.getUid(), GlobalConst.UserStatus.ONLINE);

        /**
         * 分发、刷新token
         */
        String newToken = JWTUtil.createLoginToken(user.getUid(),user.getId());
        Map<String,Object> map = new HashMap<>(2);
        map.put("newToken",newToken);
        map.put("user",user);
        return map;
    }

    @Override
    public void logout(String uid) {
        User user = userDao.findUserWithUid(uid);
        if(user == null){
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        redisService.hset("user_status",uid,GlobalConst.UserStatus.OFFLINE);

    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        String avatarURL = fileService.uploadAvatar(multipartFile,uid);
        if(avatarURL != null){
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(uid);
            userInfo.setIconId(avatarURL);
            userDao.updateUserInfo(userInfo);
        }else {
            throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        return GlobalConst.Path.AVATAR_URL+avatarURL;
    }

    @Override
    public boolean updateUserInfo(EditUserInfoDTO editUserInfoDTO,String uid) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setUpdateTime(new Date());
        userInfo.setDesc(editUserInfoDTO.getSignWord());
        userInfo.setIconId(editUserInfoDTO.getAvatar());
        userInfo.setGender(editUserInfoDTO.getGender());
        userInfo.setUsername(editUserInfoDTO.getUsername());

        return userDao.updateUserInfo(userInfo)>0;
    }

    @Override
    public EditUserInfoDTO getSelfInfo(Integer userId) {
        if(userId == null){
            throw new BusinessException(ExceptionType.NO_LOGIN_ERROR);
        }
        return userDao.selectSelfInfo(userId);
    }

    @Override
    public Date getUserLastLoginTime(String uid) {
        return userDao.getUserLastLoginTime(uid);
    }

    @Override
    public Integer getUserOnlineStatus(String uid) {
        Integer onlineStatus = (Integer) redisService.hget("user_status",uid);
        if(onlineStatus == null) onlineStatus = 1;
        return onlineStatus;
    }

    @Override
    public boolean isOnline(String uid) {
        if(uid==null) return false;
        Integer onlineStatus = getUserOnlineStatus(uid);
        return ! onlineStatus.equals(0);
    }

}
