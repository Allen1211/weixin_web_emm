package com.allen.imsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public interface IFileService {

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile multipartFile, String uid);
}
