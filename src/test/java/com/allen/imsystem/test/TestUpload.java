package com.allen.imsystem.test;

import com.allen.imsystem.dao.mappers.FileMapper;
import com.allen.imsystem.model.dto.FileUploadInfo;
import com.allen.imsystem.model.pojo.FileMd5;
import com.allen.imsystem.service.IFileService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    public void test() throws IOException {
        File file = new File("C:/Users/83780/Desktop/login-bg.556c92d6.png");
        BufferedImage bim = ImageIO.read(file);
        int srcWidth = bim.getWidth();
        int srcHeight = bim.getHeight();
        byte[] src = FileUtils.readFileToByteArray(file);
        System.out.println(src.length);
        byte[] image = fileService.compressImage(src,srcWidth,srcHeight,250*1024L,0.1);
        System.out.println(image.length);
        FileUtils.writeByteArrayToFile(file,image);
    }


}
