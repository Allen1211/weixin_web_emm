package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.pojo.ApplyNotify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface NotifyMapper {

    List<FriendApplicationDTO> selectNewApplyNotify(@Param("uid") String uid, @Param("type") Integer type);

    List<NewFriendNotify> selectNewFriendNotify(@Param("uid") String uid, @Param("type") Integer type);

    Integer insertNewApplyNotify(ApplyNotify applyNotify);

    Integer deleteAllNotify(@Param("type") Integer type, @Param("uid") String uid);
}
