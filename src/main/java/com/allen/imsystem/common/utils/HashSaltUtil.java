package com.allen.imsystem.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 生成加密密码
 */
public class HashSaltUtil {
    private static String alg = "MD5";

    public static String getHashSaltPwd(String password,String salt){
        return encrypted(password+salt);
    }

    private static String encrypted(String src) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] pwd = md.digest(src.getBytes());
        String pwd_str = Base64.encodeBase64String(pwd);
        return pwd_str;
    }
}
