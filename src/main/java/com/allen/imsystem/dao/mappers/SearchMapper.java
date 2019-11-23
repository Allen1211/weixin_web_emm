package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.UserSearchResult;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@Mapper
public interface SearchMapper {

    @MapKey("userInfo.uid")
    Map<String, UserSearchResult> search(String keyword);
}
