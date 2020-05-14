package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.message.NotifyPackage;
import com.allen.imsystem.common.message.Subject;
import com.allen.imsystem.mappers.FriendMapper;
import com.allen.imsystem.mappers.NotifyMapper;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.dto.UserInfoDTO;
import com.allen.imsystem.model.pojo.ApplyNotify;
import com.allen.imsystem.service.INotifyService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotifyService implements INotifyService {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private Subject notifySubject;

    @Override
    public List<NewFriendNotify> getAllNewFriendNotify(String uid) {
        return notifyMapper.selectNewFriendNotify(uid, GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
    }

    @Override
    public List<FriendApplicationDTO> getAllNewFriendApplyNotify(String uid) {
        return notifyMapper.selectNewApplyNotify(uid, GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
    }

    @Override
    public void saveAndPushNewFriendNotify(String receiverId, Integer applyId, Integer groupId) {
        ApplyNotify applyNotify = new ApplyNotify(receiverId, applyId, GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
        saveNewApplyNotify(applyNotify);
        // 推送
        if (userService.isOnline(receiverId)) {
            UserInfoDTO friendInfo = friendMapper.selectFriendInfo(receiverId);
            NewFriendNotify notifyContent = new NewFriendNotify(friendInfo, groupId);
            pushNotify(2,receiverId, notifyContent);
        }
    }

    @Override
    public void saveAndPushNewApplyNotify(String senderId, String receiverId, String reason, Integer applyId) {
        ApplyNotify applyNotify = new ApplyNotify(receiverId, applyId, GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
        saveNewApplyNotify(applyNotify);

        // 推送 新申请
        if (userService.isOnline(receiverId)) {
            UserInfoDTO applicantInfo = friendMapper.selectFriendInfo(senderId);
            FriendApplicationDTO notifyContent = new FriendApplicationDTO(reason, false, applicantInfo);
            pushNotify(1,receiverId, notifyContent);
        }
    }

    public void pushNotify(Integer type, Object receiverId, Object notifyContent) {
        NotifyPackage notifyPackage = NotifyPackage.builder()
                .type(type)
                .receiver(receiverId)
                .notifyContent(notifyContent)
                .build();
        notifySubject.notifyObserver(notifyPackage);
    }

    @Override
    public void getOfflineNotifyAndSend(String uid) {
        List<NewFriendNotify> newFriendNotifyList = getAllNewFriendNotify(uid);
        List<FriendApplicationDTO> newApplyNotifyList = getAllNewFriendApplyNotify(uid);
        pushNotify(GlobalConst.NotifyType.NEW_FRIEND_NOTIFY, uid,newFriendNotifyList);
        pushNotify(GlobalConst.NotifyType.NEW_APPLY_NOTIFY, uid,newApplyNotifyList);
    }

    @Transactional
    void saveNewApplyNotify(ApplyNotify applyNotify) {
        notifyMapper.insertNewApplyNotify(applyNotify);
    }


    @Override
    public void deleteAllNotify(Integer type, String uid) {
        notifyMapper.deleteAllNotify(type, uid);
    }
}
