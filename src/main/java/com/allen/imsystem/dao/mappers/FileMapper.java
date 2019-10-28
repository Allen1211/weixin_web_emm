package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.pojo.FileMd5;


public interface FileMapper {

    FileMd5 selectFileMd5(String md5);

    String getName(String md5);

    Integer checkMD5Exist(String md5);

    Integer insertFileMd5(FileMd5 fileMd5);


}
