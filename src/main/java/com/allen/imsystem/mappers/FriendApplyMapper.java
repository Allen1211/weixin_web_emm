package com.allen.imsystem.mappers;

import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.pojo.FriendApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName FriendApplyMapper
 * @Description 好友申请表的数据库操作
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
@Repository
@Mapper
public interface FriendApplyMapper {

    /**
     * 查询某用户已发出申请了但未被通过的好友id
     * @param uid 用户id
     * @return 未被通过的好友id列表
     */
    List<String> selectAquiredId(String uid);

    /**
     * 查询某个申请的好友分组id
     * @param fromUid 申请发出用户
     * @param toUid 申请目标用户
     * @return 该申请的好友分组id
     */
    Integer  selectApplyGroupId(@Param("fromUid") String fromUid, @Param("toUid") String toUid);

    /**
     * 查询两个用户之间的未被通过的申请
     * @param fromUid 申请发出用户
     * @param toUid 申请目标用户
     * @return 申请pojo
     */
    FriendApply selectFriendApply(@Param("fromUid") String fromUid, @Param("toUid") String toUid);

    /**
     * 查询最新的好友申请列表
     * @param uid 用户uid
     * @param limit 限制条数
     * @return 好友申请vo列表
     */
    List<FriendApplicationDTO> selectLatestApply(@Param("uid") String uid, @Param("limit") Integer limit);

    /**
     * 插入一条新的好友申请
     * @param friendApply pojo
     */
    Integer addFriendApply(FriendApply friendApply);

    /**
     * 更新申请的pass字段
     * @param pass 1->通过，0->未通过
     * @param fromUid 申请发起者
     * @param toUid 申请目标
     */
    Integer updateFriendApplyPass(@Param("pass") boolean pass, @Param("fromUid") String fromUid, @Param("toUid") String toUid);

    /**
     * 删除申请
     * @param fromUid 申请发起者
     * @param toUid 申请目标
     */
    Integer deleteFriendApply(@Param("fromUid") String fromUid, @Param("toUid") String toUid);
}
