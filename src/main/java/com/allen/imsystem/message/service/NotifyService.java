package com.allen.imsystem.message.service;

import com.allen.imsystem.friend.model.vo.FriendApplicationView;
import com.allen.imsystem.message.model.vo.NewFriendNotify;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 主要负责好友申请通知，新好友通知的发送、保存
 */
@Service
public interface NotifyService {
    /**
     * 获取某用户未送达的新好友通知
     */
    List<NewFriendNotify> getAllNewFriendNotify(String uid);
    /**
     * 获取某用户未送达的新申请通知
     */
    List<FriendApplicationView> getAllNewFriendApplyNotify(String uid);

    void saveAndPushNewFriendNotify(String receiverId, Integer applyId, Integer groupId);

    void saveAndPushNewApplyNotify(String senderId, String receiverId, String reason, Integer applyId);

    void pushNotify(Integer type, Object receiverId, Object notifyContent);

    void getOfflineNotifyAndSend(String uid);

    /**
     * 删除通知（已送达）
     * @param type
     * @param uid
     */
    void deleteAllNotify(Integer type, String uid);


//    void sendNotify(Noti)
}
