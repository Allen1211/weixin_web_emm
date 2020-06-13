package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;

public class GroupMsgTalkDataHandler extends MsgHandler {



    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
//
//        String gid = sendMsgDTO.getGid();
//
//        Set<Object> memberIdSet = groupChatService.getGroupMemberFromCache(gid);
//        memberIdSet.remove(sendMsgDTO.getSrcId());  //去掉发送者
//
//        if(CollectionUtils.isEmpty(memberIdSet)){
//            return;
//        }
//        List<Object> destIdList = new ArrayList<>(memberIdSet);
//
//        // 接收者增加一条未读信息
//        messageCounter.incrGroupChatNewMsgCount(destIdList, gid);
//
//        List<String> onlineList = new ArrayList<>(destIdList.size());
//        for (Object destIdObj : destIdList) {
//            String destId = (String) destIdObj;
//            Integer onlineStatus = userService.getUserOnlineStatus(destId);
//            if (!GlobalConst.UserStatus.OFFLINE.equals(onlineStatus)) {
//                onlineList.add(destId);
//            }
//        }
//        if (!onlineList.isEmpty()) {
//            Map<String, ChatSessionDTO> allChatData = groupChatService.getAllGroupChatSession(gid);
//                pushMessageDTO.setMessageData(msgRecord);
//                for (String destId : onlineList) {
//                    // 组装会话信息
//                    ChatSessionDTO chatSessionDTO = allChatData.get(destId);
//                    if (chatSessionDTO != null) {
//                        boolean isNewTalk = checkIsNewGroupChatAndActivate(destId, gid); //判断是否是新会话，是的话激活为显示状态
//                        pushMessageDTO.setIsNewTalk(isNewTalk);
//                        pushMessageDTO.setLastTimeStamp(chatSessionDTO.getLastMessageDate().getTime());
//                        pushMessageDTO.setChatId(chatSessionDTO.getChatId());
//                        // 未读信息数
//                        chatSessionDTO.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(destId, gid));
//                    }
//                    pushMessageDTO.setTalkData(chatSessionDTO);
//            }
//        }
    }
}
