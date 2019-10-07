package com.allen.imsystem.common.utils;

import java.util.regex.Pattern;

public class StringUtil {

    public static boolean isEmpty(String str){
        return str == null || str.length()==0;
    }

    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }


    public static boolean machesPattern(String str, Pattern pattern){
        return pattern.matcher(str).matches();
    }

    public static boolean machesPattern(String str, String pattern){
        return str.matches(pattern);
    }

    public static boolean lengthBetween(String str,Integer min, Integer max){
        return isNotEmpty(str) && str.length()>=min && str.length()<=max;
    }
    public static boolean lengthNotBetween(String str,Integer min, Integer max){
        return !(isNotEmpty(str) && str.length()>=min && str.length()<=max);
    }
}
