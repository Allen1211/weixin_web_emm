package com.allen.imsystem.id;

/**
 * @ClassName IdPoolService
 * @Description id池相关业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
public interface IdPoolService {

    /**
     * 获得一个未使用的id，并将该id删除
     * @param type 类型
     * @return 未使用的id
     */
    String nextId(int type);


    /**
     * 生成会话id
     */
    long nextChatId(int chatType);

    /**
     * 生成消息id
     */
    long nextMsgId();


}
