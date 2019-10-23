package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.MultiFileResponse;
import com.allen.imsystem.model.dto.MultipartFileDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface IFileService {

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile multipartFile, String uid);

    /**
     * 上传聊天图片
     * @param multipartFile
     * @return
     */
    String uploadMsgImg(MultipartFile multipartFile) throws IOException;

    MultiFileResponse uploadMultipartFile(MultipartFileDTO param) throws IOException;
}
