package com.allen.imsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

@SpringBootApplication
@ServletComponentScan(basePackages = "com.allen.imsystem.filter")
public class App {
    public static void main(String[] args) {
        try {

            ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
            DataSource dataSource = context.getBean(DataSource.class);

            System.out.println(dataSource.getClass());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
