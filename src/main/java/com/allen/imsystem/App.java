package com.allen.imsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan(basePackages = "com.allen.imsystem.filter")
public class App {
    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
