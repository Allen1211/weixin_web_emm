package com.allen.imsystem.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz){
        return (T) applicationContext.getBean(clazz);
    }

    public static Object getBean(String beanName){
        return applicationContext.getBean(beanName);
    }


    public static void main(String[] args) {
    }
}
