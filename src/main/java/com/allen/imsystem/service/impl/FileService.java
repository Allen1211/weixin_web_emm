package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.ByteUtil;
import com.allen.imsystem.dao.mappers.FileMapper;
import com.allen.imsystem.model.dto.FileUploadInfo;
import com.allen.imsystem.model.dto.MultiFileResponse;
import com.allen.imsystem.model.dto.MultipartFileDTO;
import com.allen.imsystem.service.IFileService;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileService implements IFileService {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        checkImageFile(multipartFile);

        String linuxPath = GlobalConst.Path.AVATAR_PATH;
        String type = getFileType(multipartFile);

        String nameDotType = uid + "." + type;
        File file = new File(linuxPath + nameDotType);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
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
        boolean hasAlreadySave = fileMapper.checkMD5Exist(md5) > 0;
        if (!hasAlreadySave) {
            String linuxPath = GlobalConst.Path.MSG_IMG_PATH;
            File file = new File(linuxPath + nameDotType);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bfo = new BufferedOutputStream(fos);
            // 图片压缩
            compressImage(multipartFile.getInputStream(), bfo, multipartFile.getSize());
            // IO写入服务器硬盘
            bfo.flush();
            // 将MD5信息写入数据库
            new Thread(() -> {
                fileMapper.insertFileMd5(md5);
            }).start();
        }
        return GlobalConst.Path.MSG_IMG_URL + nameDotType;

    }

    public MultiFileResponse uploadMultipartFile(MultipartFileDTO param) throws IOException {
        // 约定的每一块的固定大小
        long blockSize = GlobalConst.BLOCK_SIZE;
        String type = getFileType(param.getFileItem());
        String tempDirPath = GlobalConst.Path.MSG_FILE_PATH + param.getMd5();  // 该文件保存的文件夹，以md5作为文件夹名
        String tempFileName = "tmp_" + param.getMd5() + "." + type;   // 临时文件名
        File confFile = new File(tempDirPath, param.getMd5() + ".conf");   // 标识上传进度的文件
        File tmpDir = new File(tempDirPath);
        File tmpFile = new File(tempDirPath, tempFileName);
        if (!tmpDir.exists()) { // 如果文件夹不存在，新创建一个。
            tmpDir.mkdirs();
        }

        RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
        RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw");

//        FileChannel tempFileChannel = accessTmpFile.getChannel();
//        FileChannel confFileChannel = accessConfFile.getChannel();

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
        responseDTO.setMd5(param.getMd5());
        responseDTO.setCurrBlock(param.getCurrBlock());
        responseDTO.setIsComplete(isComplete);

        if (isComplete) {
            String url = GlobalConst.Path.MSG_FILE_URL + param.getMd5();
            responseDTO.setDownloadUrl(url);
            // 重命名文件
            if (tmpFile.exists()) {
                File newFileName = new File(tempDirPath, param.getFileName()+"."+type);
                tmpFile.renameTo(newFileName);
            }
            System.out.println("upload complete !!");
            System.out.println(responseDTO);
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
            System.out.println("check part " + i + " complete?:" + completeList[i]);
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

        Integer blockNum = completeList.length;
        List<Integer> unCompleteBlockList = new ArrayList<>(blockNum);
        for (int i = 1; i < completeList.length ; i++) {
            //与运算, 如果有部分没有完成则相与的值必然不是全1， 加入到未完成列表中
            if((byte)(completeList[i] & Byte.MAX_VALUE) != Byte.MAX_VALUE){
                unCompleteBlockList.add(i);
            }
        }
        if(unCompleteBlockList.size() == 0){
            return new FileUploadInfo(1,GlobalConst.Path.MSG_FILE_URL + md5);
        }else{
            return new FileUploadInfo(2,unCompleteBlockList,blockNum-1);
        }
    }

    private void checkImageFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        String contentType = multipartFile.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                && !contentType.equals("image/jpg") && !contentType.equals("image/gif")) {
            throw new BusinessException(ExceptionType.FILE_TYPE_NOT_SUPPORT);
        }

    }

    private String getFileType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        String type = contentType.substring(contentType.lastIndexOf('/') + 1);
        return type;
    }


    private String getFileType(FileItem fileItem) {
        String contentType = fileItem.getContentType();
        String type = contentType.substring(contentType.lastIndexOf('/') + 1);
        return type;
    }


    private String getFileMD5(byte[] fileBytes) {
        String md5 = ByteUtil.bytes2Hex(DigestUtils.md5Digest(fileBytes));
        return md5;
    }

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
    }
}
