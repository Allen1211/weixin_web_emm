package com.allen.imsystem.message.service.pipeline;


import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.model.vo.OneToManyDiffMsgPushPacket;
import com.allen.imsystem.message.model.vo.OneToOneMsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ClassName GroupMsgHandlerEntry
 * @Description 群聊消息处理链的入口处理器 完成一些初始化工作
 * @author XianChuLun
 */
@Component
@Scope("prototype")
public class GroupMsgHandlerEntry extends GroupMsgHandler {

    @Autowired
    private GroupChatService groupChatService;

    private final IdPoolService idPoolService;

    public GroupMsgHandlerEntry() {
        this.idPoolService = BeanUtil.getBean(IdPoolService.class);
    }


    @Override
    public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        // 为发送过来的消息分配一个消息id
        SendMsgDTO sendMsgDTO = msgSendPacket.getSendMessage();
        if(sendMsgDTO.getMsgId() == null){
            sendMsgDTO.setMsgId(idPoolService.nextMsgId());
        }
        if(msgPushPacket.getPushMessageList() == null){
            String gid = sendMsgDTO.getGid();
            // 初始化destIdList，转发给其他群员
            Set<Object> memberIdSet = groupChatService.getGroupMemberFromCache(gid);
            memberIdSet.remove(sendMsgDTO.getSrcId());  //去掉发送者
            List<String> destIdList = new ArrayList<>();
            for(Object memberId : memberIdSet){
                destIdList.add((String)memberId);
            }
            msgPushPacket.setDestIdList(destIdList);
        }

    }
}
