package com.allen.imsystem.file.mappers;

import com.allen.imsystem.file.model.FileMd5;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Repository
@Mapper
public interface FileMapper {

    FileMd5 selectFileMd5(String md5);

    String getName(String md5);

    Integer checkMD5Exist(String md5);

    Integer insertFileMd5(FileMd5 fileMd5);


}
