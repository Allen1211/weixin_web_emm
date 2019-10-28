package com.allen.imsystem.common.utils;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.service.IUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
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
    private static final long DEFAULT_EXPIRE_TIME = 48 * 60 * 60 *1000;    //单位毫秒


    /**
     * 创建登录token， 保存有用户的username和userId
     * @param expireTime
     * @return
     */
    public static String createLoginToken(String uid,Integer userId, long expireTime){
        return JWT.create()
                .withHeader(createHeader())
                .withIssuer(DEFAULT_ISSUSER)
                .withIssuedAt(new Date())
                .withClaim("uid",uid)
                .withClaim("userId",userId)
                .withExpiresAt(createExpireDate(expireTime))
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
            Date current = new Date();
            boolean valid = current.before(expiresAt);
            if(valid){
                return decodedJWT.getClaims();
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

    public static void main(String[] args) {

    }


}
