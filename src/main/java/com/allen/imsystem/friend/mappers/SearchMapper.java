package com.allen.imsystem.friend.mappers;

import com.allen.imsystem.friend.model.vo.UserSearchResult;
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
