package com.allen.imsystem.dao;

import com.allen.imsystem.dao.mappers.SearchMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.UserSearchResult;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
@Repository
public class SerachDao {

    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private SqlSessionTemplate sqlSession;

    public Map<String, UserSearchResult> searchUserByKeyword(String keyword){
        return searchMapper.search(keyword);
    }




}
