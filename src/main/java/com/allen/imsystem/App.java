package com.allen.imsystem;

import com.allen.imsystem.common.utils.BeanUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.allen.imsystem.*.mappers")
@SpringBootApplication
@ServletComponentScan(basePackages = "com.allen.imsystem.common.filter")
public class App {
    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
            BeanUtil.setApplicationContext(context);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}


