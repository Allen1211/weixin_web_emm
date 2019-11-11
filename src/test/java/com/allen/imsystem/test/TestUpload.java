package com.allen.imsystem.test;

import com.allen.imsystem.dao.mappers.FileMapper;
import com.allen.imsystem.model.dto.FileUploadInfo;
import com.allen.imsystem.model.pojo.FileMd5;
import com.allen.imsystem.service.IFileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext*.xml"})
public class TestUpload {

    @Autowired
    IFileService fileService;


    @Autowired
    FileMapper fileMapper;
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

    @Test
    public void testMD5(){
        fileService.getMd5FromUrl("http://120.77.42.156:8088/imsystem/static/msg_img/99431b0322daf5c673dca126ce6b752c.jpeg");
        fileService.getMd5FromUrl("http://120.77.42.156:8088/imsystem/static/msg_file/12345/12345.jpeg");

    }

    @Test
    public void test(){
        FileMd5 fileMd5 = fileMapper.selectFileMd5("87aa2db9d44369ed2500c628421c967ffff");
        System.out.println(fileMd5);
//        String name = fileMapper.getName("87aa2db9d44369ed2500c628421c967ffff");
//        System.out.println(name);
    }

}
