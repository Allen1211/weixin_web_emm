package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.message.service.Subject;
import com.allen.imsystem.friend.mappers.FriendMapper;
import com.allen.imsystem.message.mappers.NotifyMapper;
import com.allen.imsystem.friend.model.vo.FriendApplicationView;
import com.allen.imsystem.message.model.vo.NewFriendNotify;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.message.model.pojo.ApplyNotify;
import com.allen.imsystem.message.service.NotifyService;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Subject notifySubject;

    @Override
    public List<NewFriendNotify> getAllNewFriendNotify(String uid) {
        return notifyMapper.selectNewFriendNotify(uid, GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
    }

    @Override
    public List<FriendApplicationView> getAllNewFriendApplyNotify(String uid) {
        return notifyMapper.selectNewApplyNotify(uid, GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
    }

    @Override
    public void saveAndPushNewFriendNotify(String receiverId, Integer applyId, Integer groupId) {
        ApplyNotify applyNotify = new ApplyNotify(receiverId, applyId, GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
        saveNewApplyNotify(applyNotify);
        // 推送
        if (userService.isOnline(receiverId)) {
            UserInfoView friendInfo = friendMapper.selectFriendInfo(receiverId);
            NewFriendNotify notifyContent = new NewFriendNotify(friendInfo, groupId);
            pushNotify(2, receiverId, notifyContent);
        }
    }

    @Override
    public void saveAndPushNewApplyNotify(String senderId, String receiverId, String reason, Integer applyId) {
        ApplyNotify applyNotify = new ApplyNotify(receiverId, applyId, GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
        saveNewApplyNotify(applyNotify);

        // 推送 新申请
        if (userService.isOnline(receiverId)) {
            UserInfoView applicantInfo = friendMapper.selectFriendInfo(senderId);
            FriendApplicationView notifyContent = new FriendApplicationView(reason, false, applicantInfo);
            pushNotify(1, receiverId, notifyContent);
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
        List<FriendApplicationView> newApplyNotifyList = getAllNewFriendApplyNotify(uid);
        if (!CollectionUtils.isEmpty(newFriendNotifyList)) {
            pushNotify(GlobalConst.NotifyType.NEW_FRIEND_NOTIFY, uid, newFriendNotifyList);
        }
        if (!CollectionUtils.isEmpty(newApplyNotifyList)) {
            pushNotify(GlobalConst.NotifyType.NEW_APPLY_NOTIFY, uid, newApplyNotifyList);
        }
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
