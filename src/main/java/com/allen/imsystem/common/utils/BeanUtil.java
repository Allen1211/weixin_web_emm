package com.allen.imsystem.common.utils;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @ClassName BeanUtil
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
public class BeanUtil {
    //将管理上下文的applicationContext设置成静态变量，供全局调用
    private static ConfigurableApplicationContext applicationContext;

    //定义一个获取已经实例化bean的方法
    public static <T> T getBean(Class<T> c){
        return applicationContext.getBean(c);
    }

    public static <T> T getBean(Class<T> c, Object ...args){
        return applicationContext.getBean(c, args);
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        BeanUtil.applicationContext = applicationContext;
    }
}
