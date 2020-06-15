package com.allen.imsystem.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @ClassName BeanUtil
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Component
public class BeanUtil implements ApplicationContextAware {
    //将管理上下文的applicationContext设置成静态变量，供全局调用
    private static ApplicationContext applicationContext;

    //定义一个获取已经实例化bean的方法
    public static <T> T getBean(Class<T> c){
        return applicationContext.getBean(c);
    }

    public static <T> T getBean(Class<T> c, Object ...args){
        return applicationContext.getBean(c, args);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BeanUtil.applicationContext = applicationContext;
    }
}
