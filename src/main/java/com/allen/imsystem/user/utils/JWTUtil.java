package com.allen.imsystem.user.utils;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类，具有自定义生成Token和生成默认的用于登录验证的Token的功能
 */
@Component
public class JWTUtil {

    private static RedisService redisService;

    /**
     *
     */
    /**
     * 默认密钥
     */
    private static final String DEFAULT_SECRET = "emm_imsystem";
    /**
     * 默认算法
     */
    private static final String DEFAULT_ALGORITHM_NAME = "HS256";
    private static final Algorithm DEFAULT_ALGORITHM = Algorithm.HMAC256(DEFAULT_SECRET);

    /**
     * 默认发布人
     */
    private static final String DEFAULT_ISSUSER = "imsystem.com";
    /**
     * 默认过期时间 60分钟
     */
    private static final long DEFAULT_EXPIRE_TIME = 7 * 24 * 60 * 60 *1000;    //过期时间7天


    /**
     * 创建登录token， 保存有用户的username和userId
     * @param expireTime
     * @return
     */
    public static String createLoginToken(String uid,Integer userId, long expireTime){
        String editionCode = UUID.randomUUID().toString();
        return JWT.create()
                .withHeader(createHeader())
                .withIssuer(DEFAULT_ISSUSER)
                .withIssuedAt(new Date())
                .withClaim("uid",uid)   // 用户账号
                .withClaim("userId",userId) // 数据库主键
                .withClaim("editionCode",editionCode)   // 版本号，用于黑名单失效
                .withExpiresAt(createExpireDate(expireTime))    // 过期时间 2天
                .sign(DEFAULT_ALGORITHM);
    }

    /**
     * 创建登录token，未指定过期时间，使用默认过期时间
     * @param uid
     * @return
     */
    public static String createLoginToken(String uid,Integer userId){
        return createLoginToken(uid,userId,DEFAULT_EXPIRE_TIME);
    }


    public static <T> T getMsgFromToken(String token, String name, Class<T> clazz){
        try {
            JWTVerifier verifier = JWT.require(DEFAULT_ALGORITHM).withIssuer(DEFAULT_ISSUSER).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim(name).as(clazz);
        }catch(JWTVerificationException e){
            throw new BusinessException(ExceptionType.TOKEN_EXPIRED_ERROR);
        }
    }

    /**
     * 校验登录token
     * @param token
     * @return 校验成功返回解密出的参数
     */
    public static Map<String, Claim> verifyLoginToken(String token, String correctUid){
        try {
            JWTVerifier verifier = JWT.require(DEFAULT_ALGORITHM).withIssuer(DEFAULT_ISSUSER).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            Map<String, Claim> claimMap = decodedJWT.getClaims();
            String editionCode = claimMap.get("editionCode").asString();    // 验证版本号是否在黑名单里，若在，验证失败
            if(new Date().before(expiresAt) && ! isInBlackList(editionCode)){
                return claimMap;
            }else{
                return null;
            }
        }catch (JWTVerificationException exception){
            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将一个token放入黑名单
     */
    public static boolean addTokenToBlackList(HttpServletRequest request){
        String token =  getTokenFromRequest(request);
        return addTokenToBlackList(token);
    }
    private static boolean addTokenToBlackList(String token){
        JWTVerifier verifier = JWT.require(DEFAULT_ALGORITHM).withIssuer(DEFAULT_ISSUSER).build();
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            Map<String, Claim> claimMap = decodedJWT.getClaims();
            String editionCode = claimMap.get("editionCode").asString();
            Long expiresAt = decodedJWT.getExpiresAt().getTime();   // 过期时间作为scope，便于定时删除过期的黑名单
            return redisService.zSetAdd(GlobalConst.RedisKey.KEY_TOKEN_BLACKLIST,editionCode,expiresAt.doubleValue());
        }catch (Exception e){
            // 如果现在验证已经不通过（只有过期的情况） 就不用添加了
            return true;
        }
    }

    private static boolean isInBlackList(String editionCode){
        return redisService.zSetHasMember(GlobalConst.RedisKey.KEY_TOKEN_BLACKLIST,editionCode);
    }

    public static Long cleanExpiresBalkList(){
        Long now = System.currentTimeMillis();
        double scope = now.doubleValue();
        return redisService.zRemoveRangeByScore(GlobalConst.RedisKey.KEY_TOKEN_BLACKLIST,scope);
    }

    private static String getTokenFromRequest(HttpServletRequest request){
        String token = request.getHeader("token");
        return token;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        JWTUtil.redisService = redisService;
    }

    private static Map<String,Object> createHeader(){
        Map<String ,Object> header = new HashMap<>(2);
        header.put("typ","JWT");
        header.put("alg",DEFAULT_ALGORITHM_NAME);
        return header;
    }
    private static Date createExpireDate(){
        return new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_TIME);
    }
    private static Date createExpireDate(long expireTime){
        return new Date(System.currentTimeMillis() + expireTime);
    }


}
