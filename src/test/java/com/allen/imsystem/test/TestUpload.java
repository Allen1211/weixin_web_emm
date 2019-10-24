package com.allen.imsystem.test;

import com.allen.imsystem.model.dto.FileUploadInfo;
import com.allen.imsystem.service.IFileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestUpload {

    @Autowired
    IFileService fileService;

    public static void main(String[] args) throws IOException {
        String linuxPath = "/usr/resources/imsystem/static/avatar/";
//        String linuxPath = "C:/Users/83780/Desktop/";
        String filePath = linuxPath + "123456.txt";
        File file = new File(filePath);
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        String text = "Hello Linux";
        os.write(text.getBytes("UTF-8"));
        os.flush();
    }

    @Test
    public void testFile() throws IOException {
        FileUploadInfo f = fileService.getUnCompleteParts("12345");
        System.out.println(f);
    }

}
