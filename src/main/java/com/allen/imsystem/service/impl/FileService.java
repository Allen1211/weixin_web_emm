package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.ByteUtil;
import com.allen.imsystem.dao.mappers.FileMapper;
import com.allen.imsystem.service.IFileService;
import jdk.nashorn.internal.objects.Global;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class FileService implements IFileService {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        checkImageFile(multipartFile);

        String linuxPath = GlobalConst.Path.AVATAR_PATH;
        String type = getFileType(multipartFile);

        String nameDotType = uid+"."+type;
        File file = new File(linuxPath+nameDotType);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(),file);
        }catch(IOException e){
            e.printStackTrace();
            throw new BusinessException(ExceptionType.SERVER_ERROR, "无法打开文件输入流");
        }

        return nameDotType;
    }


    public String uploadMsgImg(MultipartFile multipartFile) throws IOException {
        checkImageFile(multipartFile);
        // 1、 计算图片md5
        String md5 = getFileMD5(multipartFile.getBytes());
        String type = getFileType(multipartFile);
        String nameDotType = md5 + "." + type;
        // TODO 2、 到数据库查询该MD5是否已经存在
        boolean hasAlreadySave = fileMapper.checkMD5Exist(md5)>0;
        if(!hasAlreadySave){
            String linuxPath = GlobalConst.Path.MSG_IMG_PATH;
            File file = new File(linuxPath + nameDotType);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bfo = new BufferedOutputStream(fos);
            // 图片压缩
            compressImage(multipartFile.getInputStream(),bfo,multipartFile.getSize());
            // IO写入服务器硬盘
            bfo.flush();
            // 将MD5信息写入数据库
            new Thread(()->{
                fileMapper.insertFileMd5(md5);
            }).start();
        }
        return GlobalConst.Path.MSG_IMG_URL + nameDotType;

    }

    private void checkImageFile(MultipartFile multipartFile){
        if(multipartFile.isEmpty()){
            throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        String contentType = multipartFile.getContentType();
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                && !contentType.equals("image/jpg") && !contentType.equals("image/gif")){
            throw new BusinessException(ExceptionType.FILE_TYPE_NOT_SUPPORT);
        }

    }

    private String getFileType(MultipartFile multipartFile){
        String contentType = multipartFile.getContentType();
        String type = contentType.substring(contentType.lastIndexOf('/')+1);
        return type;
    }


    private String getFileMD5(byte[] fileBytes){
        String md5 = ByteUtil.bytes2Hex(DigestUtils.md5Digest(fileBytes));
        return md5;
    }

    private void compressImage(InputStream is, OutputStream os, Long size) throws IOException {
        double quality = 0.5;
        if(size < 1024 * 1024){ // 1M以下
            quality = 0.75;
        }else if(size < 1024 * 1024 * 3){   // 3M以下
            quality = 0.5;
        }else{  // 3M以上
            quality = 0.25;
        }
        Thumbnails.of(is).scale(1).outputQuality(quality).toOutputStream(os);
    }
}
