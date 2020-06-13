package com.allen.imsystem.message.mappers;

import com.allen.imsystem.common.bean.PageBean;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 私聊信息的数据库表操作类
 */
@Mapper
@Repository
public interface PrivateMsgRecordMapper {

    /**
     * 根据消息id查询消息
     * @param msgId 消息id
     */
    PrivateMsgRecord findById(Long msgId);

    /**
     * 插入一条私聊消息
     * @param privateMsgRecord 私聊消息实体类
     */
    int insert(PrivateMsgRecord privateMsgRecord);

    /**
     * 更新一条私聊消息
     * @param privateMsgRecord 私聊消息实体类
     */
    int updateById(PrivateMsgRecord privateMsgRecord);

    /**
     * 删除一条私聊消息
     * @param msgId 消息id
     */
    int deleteById(Long msgId);

    /**
     * 查询私聊会话的聊天记录
     * @param chatId 会话id
     * @param beginMsgId 从哪一条消息id开始查找
     * @param uid 用户id
     * @param pageBean 分页实体类
     * @return 聊天记录列表
     */
    List<MsgRecord> findMsgRecordList(@Param("chatId") Long chatId, @Param("beginMsgId") Long beginMsgId,
                                      @Param("uid") String uid, @Param("pageBean") PageBean pageBean);

    /**
     * 统计某个会话聊天记录的总条数
     * @param chatId 会话id
     * @param beginTime 开始时间
     * @return 聊天记录的总条数
     */
    Integer countAllPrivateHistoryMsg(@Param("chatId") Long chatId, @Param("beginTime") Date beginTime);

    /**
     * 统计某个用户某个会话未读消息的总条数
     * @param chatId 会话id
     * @param uid 用户id
     * @return 未读消息的总条数
     */
    Integer countPrivateChatUnReadMsgForUser(@Param("chatId") Long chatId, @Param("uid") String uid);

    /**
     * 将某用户某会话的所有未读消息设为已读
     * @param chatId 会话id
     * @param uid 用户uid
     */
    int setAllPrivateChatMsgHasRead(@Param("chatId") Long chatId, @Param("uid") String uid);
}