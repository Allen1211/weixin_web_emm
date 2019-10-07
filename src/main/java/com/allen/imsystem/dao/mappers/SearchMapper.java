package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.UserSearchResult;
import org.apache.ibatis.annotations.MapKey;

import java.util.Map;

public interface SearchMapper {

    @MapKey("userInfo.uid")
    Map<String, UserSearchResult> search(String keyword);
}
