package com.allen.imsystem.message.mappers;

import com.allen.imsystem.common.bean.PageBean;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @ClassName GroupMsgRecordMapper
 * @Description 私聊信息的数据库表操作类
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Mapper
@Repository
public interface GroupMsgRecordMapper {
    /**
     * 查询群会话聊天记录
     * @param gid 群id
     * @param beginMsgId 从哪条消息开始查询
     * @param uid 用户uid
     * @param pageBean 分页参数
     * @return 聊天记录
     */
    List<MsgRecord> selectGroupChatHistoryMsg(@Param("gid") String gid, @Param("beginMsgId") Long beginMsgId,
                                              @Param("uid") String uid, @Param("pageBean") PageBean pageBean);

    /**
     * 查询某群聊会话聊天记录总条数
     * @param gid 群id
     * @param beginTime 开始时间
     * @return 聊天记录总条数
     */
    Integer countAllGroupHistoryMsg(@Param("gid") String gid, @Param("beginTime") Date beginTime);

    /**
     * 插入一条新的群聊消息
     * @param groupMsgRecord 群聊消息
     */
    int insertNewGroupMsgRecord(GroupMsgRecord groupMsgRecord);

    /**
     * 插入多条新的群聊消息
     * @param msgList 群聊消息
     */
    int insertNewGroupMsgRecordBatch(@Param("msgList") List<GroupMsgRecord> msgList);
}
