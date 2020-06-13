package com.allen.imsystem.id;

import com.allen.imsystem.user.mappers.IdPoolMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.allen.imsystem.common.Const.GlobalConst.*;

/**
 * @ClassName IdPoolImpl
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Service
public class IdPoolServiceImpl implements IdPoolService {

    @Autowired
    private IdPoolMapper idPoolMapper;

    /**
     * 获得一个未使用的id，并将该id删除
     *
     * @param type 类型
     * @return 未使用的id
     *
     * 事务隔离等级设置成RC，这样保证该事务的删除操作未提交时，另外一个事务不会读到脏数据
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public synchronized String nextId(int type) {
        String id = null;
        switch (type) {
            case IdType.UID:
                UidPool uidPool = idPoolMapper.selectNextUnUsedUid();
                id = uidPool.getUid();
                break;
            case IdType.GID:
                id = idPoolMapper.selectUnUsedGid();
                break;
        }
        // 取出后立即删除
        if (id != null) {
            deleteUsedId(type, id);
        }
        return id;
    }

    /**
     * 从id池删除一个使用了的id
     *
     * @param id 使用了的id
     */
    private synchronized boolean deleteUsedId(int type, String id) {
        switch (type) {
            case IdType.UID:
                return idPoolMapper.sortDeleteUsedUid(id) > 0;
            case IdType.GID:
                return idPoolMapper.softDeleteUsedGid(id) > 0;
        }
        return false;
    }

    /**
     * 生成会话id
     *
     * @param chatType
     */
    @Override
    public long nextChatId(int chatType) {
        return ChatIdUtil.generate(chatType);
    }

    /**
     * 生成消息id
     */
    @Override
    public long nextMsgId() {
        return SnowFlakeUtil.getNextSnowFlakeId();
    }

    private void generateId(){
        Set<String> set = new HashSet<>(10000);
        StringBuilder base = new StringBuilder("60806040");
        for(int i=0;i<=9999;i++){
            char[] seq = String.format("%04d",i).toCharArray();
            base.setCharAt(1,seq[0]);
            base.setCharAt(3,seq[1]);
            base.setCharAt(5,seq[2]);
            base.setCharAt(7,seq[3]);
            set.add(base.toString());
        }
        List<String> list = new ArrayList<>(set);
        int affected = idPoolMapper.insertBatchIntoUidPool(list);
//        int affected = idPoolMapper.insertBatchIntoGidPool(list);
        System.out.println(affected);
    }
}
