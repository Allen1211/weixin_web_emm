package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.ParamValidator;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import com.allen.imsystem.service.IUserService;
import com.allen.imsystem.common.utils.HashSaltUtil;
import com.allen.imsystem.common.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisUtil redisUtil;

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
         * 生成8位纯数字账号，不与已有的账号重复
         */
        String uid;
        Random random = new Random(System.currentTimeMillis());
        do{
            int randomId = random.nextInt(89999999) + 10000000 ;
            uid = String.valueOf(randomId);
        }while (!userDao.countUid(uid).equals(0));


        //对密码加密
        String salt = UUID.randomUUID().toString();
        password = HashSaltUtil.getHashSaltPwd(password,salt);

        // 插入数据库
        User user = new User(uid,password,salt,email);
        UserInfo userInfo = new UserInfo(uid,username);
        userInfo.setIconId(userInfo.getUid());

        userDao.insertUser(user);
        userDao.insertUserInfo(userInfo);

        return;
    }

    /**
     * 账号密码登录， 成功返回token，失败抛出业务异常
     * @param password
     * @return
     */
    @Override
    public Map<String,String> login(String uidOrEmail, String password){
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
        if(!hashPassword.equals(HashSaltUtil.getHashSaltPwd(password,salt)))
            throw new BusinessException(ExceptionType.USERNAME_PASSWORD_ERROR);

        //TODO LOG

        // redis更新该用户的在线状态 至在线
        redisUtil.hset("user_status",user.getUid(), GlobalConst.UserStatus.ONLINE);

        /**
         * 分发、刷新token
         */
        String newToken = JWTUtil.createLoginToken(user.getUid());
        String uid = user.getUid();
        Map<String,String> map = new HashMap<>(2);
        map.put("newToken",newToken);
        map.put("uid",uid);
        return map;
    }

    @Override
    public void logout(String uid) {
        User user = userDao.findUserWithUid(uid);
        if(user == null){
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        redisUtil.hset("user_status",uid,GlobalConst.UserStatus.OFFLINE);
    }

}
