package com.allen.imsystem.chat.mappers.group;

import com.allen.imsystem.chat.model.pojo.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @ClassName GroupMapper
 * @Description 群聊的数据库操作类
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Mapper
@Repository
public interface GroupMapper {

    /**
     * 查询群实体
     * @param gid 群号
     */
    Group findByGId(@Param("gid")String gid);

    /**
     * 插入一个新群
     * @param group 群
     */
    int insert(Group group);

    /**
     * 更新群聊
     * @param group 群聊
     */
    int update(Group group);

    /**
     * 删除群聊
     * @param gid 群id
     */
    int delete(String gid);

    /**
     * 查询群聊的状态
     * @param gid 群id
     * @return true: 有效 false: 已解散
     */
    boolean selectGroupChatStatus(String gid);

    /**
     * 查询群的群主uid
     * @param gid 群id
     * @return 群主uid
     */
    String selectGroupOwnerId(String gid);

    /**
     * 更新群聊最后一条消息
     * @param gid 群id
     * @param lastMsgId 最后一条消息id
     * @param lastSenderId 最后一个发送者id
     */
    int updateGroupLastMsg(@Param("gid") String gid, @Param("lastMsgId") Long lastMsgId,
                           @Param("lastMsgContent") String lastMsgContent,
                           @Param("lastMsgCreateTime") Date lastMsgCreateTime,
                           @Param("lastSenderId") String lastSenderId);
}
