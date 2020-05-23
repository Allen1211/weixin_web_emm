package com.allen.imsystem.mappers;

import com.allen.imsystem.model.dto.FriendGroup;
import com.allen.imsystem.model.pojo.FriendGroupPojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName FriendGroup
 * @Description 好友分组表的数据库操作
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
@Repository
@Mapper
public interface FriendGroupMapper {

    /**
     * 查询某用户的好友分组列表
     * @param uid 用户uid
     * @return 好友分组列表
     */
    List<FriendGroup> selectFriendGroupList(String uid);

    /**
     * 根据某用户的好友分组名查询该分组的id
     * @param uid 用户uid
     * @param groupName 好友分组名
     * @return 好友分组id
     */
    Integer selectGroupId(@Param("uid") String uid, @Param("groupName") String groupName);

    /**
     * 查询某个好友分组的状态
     * @param groupId 分组id
     * @return 是否有效
     */
    Boolean isGroupValid(Integer groupId);

    /**
     * 查询好友分组列表，并且统计每一个分组的好友数
     * @param uid 用户uid
     * @return 带好友数的分组列表
     */
    List<FriendGroup> selectFriendGroupListWithSize(@Param("uid") String uid);

    /**
     * 统计此好友分组的好友数
     * @param groupId 好友分组id
     * @param uid 用户uid
     * @return 该分组的好友数
     */
    Integer selectGroupSize(@Param("groupId") Integer groupId, @Param("uid") String uid);

    /**
     * 查询某用户的默认好友分组
     * @param uid 好友uid
     * @return 默认分组
     */
    FriendGroupPojo selectUserDefaultFriendGroup(@Param("uid") String uid);

    /**
     * 更新好友分组名
     * @param groupId 分组id
     * @param groupName 分组名
     * @param uid 用户uid
     */
    Integer updateFriendGroupName(@Param("groupId") Integer groupId, @Param("groupName") String groupName,
                                  @Param("uid") String uid);

    /**
     * 删除好友分组
     * @param groupId 好友分组id
     * @param uid 用户uid
     */
    Integer deleteFriendGroup(@Param("groupId") Integer groupId, @Param("uid") String uid);

    /**
     * 把某个分组的所有好友移到默认分组
     * @param defaultGroupId 默认分组id
     * @param groupId 源分组
     * @param uid 用户uid
     */
    Integer moveGroupFriendToDefaultGroup(@Param("defaultGroupId") Integer defaultGroupId, @Param("groupId") Integer groupId, @Param("uid") String uid);

    /**
     * 把某个好友移到另一个分组
     * @param uid 用户uid
     * @param friendId 好友uid
     * @param oldGroupId 旧分组id
     * @param newGroupId 新分组id
     */
    Integer moveFriendToAnotherGroup(@Param("uid") String uid, @Param("friendId") String friendId,
                                     @Param("oldGroupId") Integer oldGroupId, @Param("newGroupId") Integer newGroupId);

    /**
     * 插入新的好友分组
     * @param uid 用户id
     * @param groupName 分组名
     * @param isDefault 是否默认分组
     */
    Integer insertNewFriendGroup(@Param("uid") String uid,
                                 @Param("groupName") String groupName, @Param("isDefault") Boolean isDefault);

    int insert(FriendGroupPojo friendGroupPojo);

    int updateById(FriendGroupPojo friendGroupPojo);

    FriendGroupPojo selectById(@Param("id") Integer friendGroupId);

    List<FriendGroupPojo> selectAll();
}
