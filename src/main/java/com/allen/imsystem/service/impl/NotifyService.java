package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.dao.mappers.NotifyMapper;
import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.pojo.ApplyNotify;
import com.allen.imsystem.service.INotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotifyService implements INotifyService {

    @Autowired
    private NotifyMapper notifyMapper;


    @Override
    public List<NewFriendNotify> getAllNewFriendNotify(String uid) {
        return notifyMapper.selectNewFriendNotify(uid, GlobalConst.NotifyType.NEW_FRIEND_NOTIFY);
    }

    @Override
    public List<FriendApplicationDTO> getAllNewFriendApplyNotify(String uid) {
        return notifyMapper.selectNewApplyNotify(uid, GlobalConst.NotifyType.NEW_APPLY_NOTIFY);
    }

    @Override
    @Transactional
    public void saveNewApplyNotify(Integer type, Integer applyId, String uid) {
        saveNewApplyNotify(new ApplyNotify(uid,applyId,type));
    }

    @Override
    public void saveNewApplyNotify(ApplyNotify applyNotify) {
        notifyMapper.insertNewApplyNotify(applyNotify);
    }


    @Override
    public void deleteAllNotify(Integer type,String uid) {
        notifyMapper.deleteAllNotify(type,uid);
    }
}
