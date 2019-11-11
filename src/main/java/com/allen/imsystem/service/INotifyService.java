package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.pojo.ApplyNotify;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 主要负责好友申请通知，新好友通知的发送、保存
 */
@Service
public interface INotifyService {
    /**
     * 获取某用户未送达的新好友通知
     */
    List<NewFriendNotify> getAllNewFriendNotify(String uid);
    /**
     * 获取某用户未送达的新申请通知
     */
    List<FriendApplicationDTO> getAllNewFriendApplyNotify(String uid);

    /**
     * 保存新申请通知到数据库
     */
    void saveNewApplyNotify(ApplyNotify applyNotify);

    void saveNewApplyNotify(Integer type, Integer applyId, String uid);

    /**
     * 删除通知（已送达）
     * @param type
     * @param uid
     */
    void deleteAllNotify(Integer type, String uid);


}
