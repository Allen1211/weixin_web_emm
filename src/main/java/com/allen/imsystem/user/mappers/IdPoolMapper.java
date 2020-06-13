package com.allen.imsystem.user.mappers;

import com.allen.imsystem.id.UidPool;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName IdPoolMapper
 * @Description id池的数据库操作类
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Repository
@Mapper
public interface IdPoolMapper {
    /**
     * 查询下一个未使用的群id
     * @return 一个未使用的群id
     */
    String selectUnUsedGid();

    /**
     * 逻辑删除使用过的群id
     * @param gid 群id
     */
    int softDeleteUsedGid(String gid);

    /**
     * 查询下一个未使用的用户uid
     */
    UidPool selectNextUnUsedUid();

    /**
     * 批量插入uid到uid池
     */
    Integer insertBatchIntoUidPool(List<String> list);

    /**
     * 批量插入gid到gid池
     */
    Integer insertBatchIntoGidPool(List<String> list);

    /**
     * 逻辑删除使用过的uid
     */
    Integer sortDeleteUsedUid(String uid);
}
