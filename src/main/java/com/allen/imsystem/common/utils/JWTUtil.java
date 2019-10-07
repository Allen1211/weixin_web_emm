package com.allen.imsystem.common.utils;

import com.allen.imsystem.service.IUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，具有自定义生成Token和生成默认的用于登录验证的Token的功能
 */
public class JWTUtil {

    /**
     *
     */
    private static IUserService userService = SpringBeanUtil.getBean(IUserService.class);
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
    private static final long DEFAULT_EXPIRE_TIME = 60 * 60 *1000;    //单位毫秒


    /**
     * 创建登录token， 保存有用户的username和userId
     * @param expireTime
     * @return
     */
    public static String createLoginToken(String uid, long expireTime){
        return JWT.create()
                .withHeader(createHeader())
                .withIssuer(DEFAULT_ISSUSER)
                .withIssuedAt(new Date())
                .withClaim("uid",uid)
                .withExpiresAt(createExpireDate(expireTime))
                .sign(DEFAULT_ALGORITHM);
    }

    /**
     * 创建登录token，未指定过期时间，使用默认过期时间
     * @param uid
     * @return
     */
    public static String createLoginToken(String uid){
        return createLoginToken(uid,DEFAULT_EXPIRE_TIME);
    }

    /**
     * 创建任意的token
     * @param claims
     * @param expireTime
     * @return
     */
    public static String createCommonToken(Map<String, String> claims, long expireTime){
        JWTCreator.Builder builder =  JWT.create()
                                        .withHeader(createHeader())
                                        .withIssuer(DEFAULT_ISSUSER)
                                        .withIssuedAt(new Date());
        for(String key :claims.keySet()){
            builder.withClaim(key, claims.get(key));
        }
        return builder.withExpiresAt(createExpireDate(expireTime)).sign(DEFAULT_ALGORITHM);
    }

    /**
     * 校验登录token
     * @param token
     * @return 校验成功返回true
     */
    public static boolean verifyLoginToken(String token){
        try {
            JWTVerifier verifier = JWT.require(DEFAULT_ALGORITHM).withIssuer(DEFAULT_ISSUSER).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            Date current = new Date();
            return current.before(expiresAt);
        }catch (JWTVerificationException exception){
            return false;
        }
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
