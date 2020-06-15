package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.message.service.impl.GroupNotifyFactory;
import com.allen.imsystem.message.service.impl.GroupNotifyMsgRecordFactory;
import com.allen.imsystem.message.service.impl.NormalMsgRecordFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MsgHandlerFactory
 * @Description 负责组合并创建各种业务的处理链
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class MsgHandlerFactory {

    private final static Map<Integer, MsgHandler> instanceMap = new HashMap<>(3);

    public static class MsgHandlerType {
        public static final int PRIVATE_MSG = 1;
        public static final int GROUP_MSG = 2;
        public static final int GROUP_NOTIFY = 3;

    }

    static {
        instanceMap.put(MsgHandlerType.PRIVATE_MSG, createInstance(MsgHandlerType.PRIVATE_MSG));
        instanceMap.put(MsgHandlerType.GROUP_MSG, createInstance(MsgHandlerType.GROUP_MSG));
        instanceMap.put(MsgHandlerType.GROUP_NOTIFY, createInstance(MsgHandlerType.GROUP_NOTIFY));
    }

    public static MsgHandler getInstance(int type){
        return instanceMap.get(type);
    }

    private static MsgHandler createInstance(int type) {
        switch (type) {
            case MsgHandlerType.PRIVATE_MSG:
                return createPrivateMsgHandlerLink();
            case MsgHandlerType.GROUP_MSG:
                return createGroupMsgHandlerLink();
            case MsgHandlerType.GROUP_NOTIFY:
                return createGroupNotifyMsgHandlerLink();
            default:
                return null;
        }
    }

    /**
     * 创建私聊消息处理链
     *
     * @return 处理链入口
     */
    private static MsgHandler createPrivateMsgHandlerLink() {
        MsgHandler handlerEntry = BeanUtil.getBean(PriMsgHandlerEntry.class);    // 处理链入口
        MsgHandler priMsgSaveHandler = BeanUtil.getBean(PriMsgSaveHandler.class); // 消息入库
        MsgHandler serverAckHandler = BeanUtil.getBean(ServerAckHandler.class);   // 服务端确认回执
        MsgHandler msgOnlineFilterHandler = BeanUtil.getBean(MsgOnlineFilterHandler.class);   //在线用户推送过滤
        MsgHandler priMsgTalkDataHandler = BeanUtil.getBean(PriMsgTalkDataHandler.class); // 消息回话数据填充
        MsgHandler msgRecordPackHandler =
                BeanUtil.getBean(MsgRecordPackHandler.class, BeanUtil.getBean(NormalMsgRecordFactory.class));   // 消息内容填充
        handlerEntry.nextHandler(priMsgSaveHandler)
                .nextHandler(serverAckHandler)
                .nextHandler(msgOnlineFilterHandler)
                .nextHandler(msgRecordPackHandler)
                .nextHandler(priMsgTalkDataHandler);

        return handlerEntry;
    }

    /**
     * 创建群聊消息处理链
     *
     * @return 处理链入口
     */
    private static MsgHandler createGroupMsgHandlerLink() {
        MsgHandler handlerEntry = BeanUtil.getBean(GroupMsgHandlerEntry.class);    // 处理链入口
        MsgHandler priMsgSaveHandler = BeanUtil.getBean(GroupMsgSaveHandler.class); // 消息入库
        MsgHandler serverAckHandler = BeanUtil.getBean(ServerAckHandler.class);   // 服务端确认回执
        MsgHandler msgOnlineFilterHandler = BeanUtil.getBean(MsgOnlineFilterHandler.class);   //在线用户推送过滤
        MsgHandler msgRecordPackHandler =
                BeanUtil.getBean(MsgRecordPackHandler.class, BeanUtil.getBean(NormalMsgRecordFactory.class));   // 消息内容填充
        MsgHandler groupMsgTalkDataHandler = BeanUtil.getBean(GroupMsgTalkDataHandler.class); // 消息会话数据填充

        handlerEntry.nextHandler(priMsgSaveHandler)
                .nextHandler(serverAckHandler)
                .nextHandler(msgOnlineFilterHandler)
                .nextHandler(msgRecordPackHandler)
                .nextHandler(groupMsgTalkDataHandler);

        return handlerEntry;
    }

    /**
     * 创建群通知消息处理链
     *
     * @return 处理链入口
     */
    private static MsgHandler createGroupNotifyMsgHandlerLink() {
        MsgHandler priMsgSaveHandler = BeanUtil.getBean(GroupMsgSaveHandler.class); // 消息入库
        MsgHandler msgOnlineFilterHandler = BeanUtil.getBean(MsgOnlineFilterHandler.class);   //在线用户推送过滤
        MsgHandler msgRecordPackHandler =
                BeanUtil.getBean(MsgRecordPackHandler.class, BeanUtil.getBean(GroupNotifyMsgRecordFactory.class));   // 消息内容填充
        MsgHandler groupMsgTalkDataHandler = BeanUtil.getBean(GroupMsgTalkDataHandler.class); // 消息会话数据填充

        priMsgSaveHandler.nextHandler(priMsgSaveHandler)
                .nextHandler(msgOnlineFilterHandler)
                .nextHandler(msgRecordPackHandler)
                .nextHandler(groupMsgTalkDataHandler);

        return priMsgSaveHandler;
    }


}
