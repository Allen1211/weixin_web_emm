package com.allen.imsystem.test.friend;

import com.allen.imsystem.common.cache.CacheExecutor;
import com.allen.imsystem.common.cache.impl.FriendCache;
import com.allen.imsystem.common.cache.impl.GroupCache;
import com.allen.imsystem.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @ClassName TestFriendCache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/19
 * @Version 1.0
 */
public class TestFriendCache extends BaseTest {

    @Autowired
    FriendCache friendCache;

    @Autowired
    GroupCache groupCache;

    @Test
    public void testGet(){
        Set<String> ids = CacheExecutor.get(friendCache.friendSet, "12345678");
        System.out.println(ids);
        CacheExecutor.remove(friendCache.friendSet, "12345678","66666666");
        ids = CacheExecutor.get(friendCache.friendSet, "12345678");
        System.out.println(ids);
        System.out.println(CacheExecutor.exist(friendCache.friendSet, "12345678","66666666"));
        System.out.println(CacheExecutor.exist(friendCache.friendSet, "12345678","63374315"));
        CacheExecutor.add(friendCache.friendSet, "12345678","66666666");
        ids = CacheExecutor.get(friendCache.friendSet, "12345678");
        System.out.println(ids);
    }

    @Test
    public void testGroupCache(){
        System.out.println(CacheExecutor.exist(groupCache.membersCache,"128963402", "12345678"));
    }
}
