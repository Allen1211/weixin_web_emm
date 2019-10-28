package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.ByteUtil;
import com.allen.imsystem.dao.mappers.FileMapper;
import com.allen.imsystem.model.dto.FileUploadInfo;
import com.allen.imsystem.model.dto.MultiFileResponse;
import com.allen.imsystem.model.dto.MultipartFileDTO;
import com.allen.imsystem.model.pojo.FileMd5;
import com.allen.imsystem.service.IFileService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class FileService implements IFileService {

    @Autowired
    RedisService redisService;

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        checkImageFile(multipartFile);

        String linuxPath = GlobalConst.Path.AVATAR_PATH;
        String type = getFileType(multipartFile);

        StringBuilder nameDotType = new StringBuilder();
        nameDotType.append(uid).append('.').append(type);
        File file = new File(linuxPath + nameDotType.toString());
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ExceptionType.SERVER_ERROR, "无法打开文件输入流");
        }

        return nameDotType.toString();
    }


    public String uploadMsgImg(MultipartFile multipartFile) throws IOException {
        checkImageFile(multipartFile);
        // 1、 计算图片md5
        String md5 = getFileMD5(multipartFile.getBytes());
        String type = getFileType(multipartFile);
        StringBuilder nameDotType = new StringBuilder(md5).append('.').append(type);
        StringBuilder fullPath = new StringBuilder(GlobalConst.Path.MSG_IMG_URL).append(nameDotType);
        Long size = multipartFile.getSize();
        // TODO 2、 到数据库查询该MD5是否已经存在
        boolean hasAlreadySave = fileMapper.checkMD5Exist(md5) > 0;
        if (!hasAlreadySave) {
            String linuxPath = GlobalConst.Path.MSG_IMG_PATH;
            File file = new File(linuxPath + nameDotType);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bfo = new BufferedOutputStream(fos);
            // 图片压缩
//            compressImage(multipartFile.getInputStream(), bfo, multipartFile.getSize());
            // IO写入服务器硬盘
            bfo.write(multipartFile.getBytes());
            bfo.flush();
            fos.close();
            bfo.close();
            multipartFile = null;
            // 将MD5信息写入数据库
            fileMapper.insertFileMd5(new FileMd5(md5,nameDotType.toString(),size, "msg_img/"+nameDotType));
        }
        return fullPath.toString();

    }

    public MultiFileResponse uploadMultipartFile(MultipartFileDTO param) throws IOException {
        // 约定的每一块的固定大小
        long blockSize = GlobalConst.BLOCK_SIZE;
        String md5 = param.getMd5();
        StringBuilder tempDirPath = new StringBuilder(GlobalConst.Path.MSG_FILE_PATH).append(md5);  // 该文件保存的文件夹，以md5作为文件夹名
        StringBuilder tempFileName = new StringBuilder(md5).append(".").append(getFileType(param.getFileName()));   // 临时文件名
        StringBuilder confFileName = new StringBuilder(md5).append(".conf");   // 临时文件名
        String tempDirStr = tempDirPath.toString();

        File tmpDir = new File(tempDirStr);
        File confFile = new File(tmpDir, confFileName.toString());   // 标识上传进度的文件
        File tmpFile = new File(tmpDir,tempFileName.toString());


        System.out.println(tmpDir);
        System.out.println(confFile);
        System.out.println(tmpFile);

        if (!tmpDir.exists()) { // 如果文件夹不存在，新创建一个。
            tmpDir.mkdirs();
        }

        RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
        RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");


        // 该分片的起始写入位置
        long offset = blockSize * (param.getCurrBlock() - 1);
        //定位到该分片的偏移量
        accessTmpFile.seek(offset);
        //写入该分片数据
        accessTmpFile.write(param.getFileItem().get());

        //把该分段标记为 true 表示完成
        System.out.println("set part " + param.getCurrBlock() + " complete");
        accessConfFile.setLength(param.getBlockNum()+1);
        accessConfFile.seek(param.getCurrBlock());
        accessConfFile.write(Byte.MAX_VALUE);

        //completeList 检查是否全部完成,如果数组里是否全部都是(全部分片都成功上传)
        byte[] completeList = FileUtils.readFileToByteArray(confFile);
        boolean isComplete = checkIsComplete(completeList);
        MultiFileResponse responseDTO = new MultiFileResponse();
        responseDTO.setMd5(md5);
        responseDTO.setCurrBlock(param.getCurrBlock());
        responseDTO.setIsComplete(isComplete);

        if (isComplete) {
            String fileName = param.getFileName();
            Long size = tmpFile.length();
            String filePath = "msg_file/" + md5 + "/" + fileName;
            String downLoadUrl = GlobalConst.Path.RESOURCES_URL + filePath;
            responseDTO.setDownloadUrl(downLoadUrl);
            // 重命名文件
            if (tmpFile.exists()) {
                File newFileName = new File(tempDirStr, fileName);
                boolean success = tmpFile.renameTo(newFileName);
            }
            System.out.println("upload complete !!");
            System.out.println(responseDTO);

            // 缓存一下文件大小
            redisService.set(md5,param.getTotalSize().toString(),60*10L);

            //TODO MD5和url的对应关系保存到数据库
            new Thread(() -> {
                fileMapper.insertFileMd5(new FileMd5(md5,fileName,size,filePath));
            }).start();
        }

        accessTmpFile.close();
        accessConfFile.close();
        System.out.println("end !!!");
        return responseDTO;

    }

    private boolean checkIsComplete(byte[] completeList) {
        byte isComplete = Byte.MAX_VALUE;
        for (int i = 1; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
            //与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
            isComplete = (byte) (isComplete & completeList[i]);
        }
        return isComplete == Byte.MAX_VALUE;
    }

    public FileUploadInfo getUnCompleteParts(String md5) {

        String dirPath = GlobalConst.Path.MSG_FILE_PATH + md5;  // 该文件保存的文件夹，以md5作为文件夹名
        String confFileName = md5 + ".conf";    // 临时文件名
        File confFile = new File(dirPath, confFileName);   // 标识上传进度的文件

        if(! confFile.exists()){
            return new FileUploadInfo(3);
        }

        byte[] completeList = null;
        try {
            completeList = FileUtils.readFileToByteArray(confFile);
        }catch (IOException e){
            e.printStackTrace();
            return new FileUploadInfo(3);
        }
        if(completeList == null || completeList.length == 0){
            return new FileUploadInfo(3);
        }

        int blockNum = completeList.length;
        List<Integer> unCompleteBlockList = new ArrayList<>(blockNum);
        for (int i = 1; i < completeList.length ; i++) {
            //与运算, 如果有部分没有完成则相与的值必然不是全1， 加入到未完成列表中
            if((byte)(completeList[i] & Byte.MAX_VALUE) != Byte.MAX_VALUE){
                unCompleteBlockList.add(i);
            }
        }
        if(unCompleteBlockList.size() == 0){
            FileMd5 fileMd5 = fileMapper.selectFileMd5(md5);
            if(fileMd5 == null){
                return new FileUploadInfo(3);
            }
            return new FileUploadInfo(1,fileMd5.getUrl());
        }else{
            return new FileUploadInfo(2,unCompleteBlockList,blockNum-1);
        }
    }

    @Override
    public String getMd5FromUrl(Integer type, String url) {
        String md5 = null;
        if(type.equals(2)){
            // http://120.77.42.156:8088/imsystem/static/msg_img/99431b0322daf5c673dca126ce6b752c.jpeg
            int begin = url.lastIndexOf(GlobalConst.Path.MSG_IMG_URL) + GlobalConst.Path.MSG_IMG_URL.length();
            int end = url.lastIndexOf('.');
            md5 = url.substring(begin,end);
        }else if(type.equals(3)){
            int begin = url.lastIndexOf(GlobalConst.Path.MSG_FILE_URL)+ GlobalConst.Path.MSG_FILE_URL.length();
            int end = url.lastIndexOf('/');
            md5 = url.substring(begin,end);
        }
        System.out.println("url:"+url);
        System.out.println("md5:"+md5);
        return md5;
    }

    private void checkImageFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        String contentType = multipartFile.getContentType();
        if (!contentType.matches("image/.+")) {
            throw new BusinessException(ExceptionType.FILE_TYPE_NOT_SUPPORT);
        }

    }

    private String getFileType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        String type = contentType.substring(contentType.lastIndexOf('/') + 1);
        return type!=null ? type:"";
    }


    private String getFileType(String fullName) {
        if(fullName == null || !fullName.contains(".")){
            return "";
        }
        String type = fullName.substring(fullName.lastIndexOf('.') + 1);
        return type!=null ? type:"";
    }


    private String getFileMD5(byte[] fileBytes) {
        String md5 = ByteUtil.bytes2Hex(DigestUtils.md5Digest(fileBytes));
        return md5;
    }

    /**
     * 图片压缩
     * @param is
     * @param os
     * @param size
     * @throws IOException
     */
    private void compressImage(InputStream is, OutputStream os, Long size) throws IOException {
        double quality = 0.5;
        if (size < 1024 * 1024) { // 1M以下
            quality = 0.75;
        } else if (size < 1024 * 1024 * 3) {   // 3M以下
            quality = 0.5;
        } else {  // 3M以上
            quality = 0.25;
        }
        Thumbnails.of(is).scale(1).outputQuality(quality).toOutputStream(os);
        is.close();
    }
}
