package com.allen.imsystem.dao.mappers;

public interface FileMapper {

    Integer checkMD5Exist(String md5);

    Integer insertFileMd5(String md5);
}
