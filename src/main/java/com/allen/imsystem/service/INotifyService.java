package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.FriendApplicationDTO;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.pojo.ApplyNotify;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface INotifyService {

    List<NewFriendNotify> getAllNewFriendNotify(String uid);

    List<FriendApplicationDTO> getAllNewFriendApplyNotify(String uid);

    void addNewApplyNotify(Integer type,Integer applyId,String uid);

    void addNewApplyNotify(ApplyNotify applyNotify);

    void deleteAllNotify(Integer type, String uid);


}
