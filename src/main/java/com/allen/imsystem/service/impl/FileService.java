package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.service.IFileService;
import jdk.nashorn.internal.objects.Global;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileService implements IFileService {


    @Override
    public String uploadAvatar(MultipartFile multipartFile, String uid) {
        String linuxPath = GlobalConst.Path.AVATAR_PATH;
        if(multipartFile.isEmpty()){
           throw new BusinessException(ExceptionType.FILE_NOT_RECEIVE);
        }
        String contentType = multipartFile.getContentType();
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                && !contentType.equals("image/jpg") && !contentType.equals("image/gif")){
            throw new BusinessException(ExceptionType.FILE_TYPE_NOT_SUPPORT);
        }

        Long size = multipartFile.getSize();
        String type = contentType.substring(contentType.lastIndexOf('/')+1);
        String nameDotType = uid+"."+type;
        File file = new File(linuxPath+nameDotType);
        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(),file);
        }catch(IOException e){
            e.printStackTrace();
            throw new BusinessException(ExceptionType.SERVER_ERROR, "无法打开文件输入流");
        }

        return GlobalConst.Path.AVATAR_URL + nameDotType;
    }
}
