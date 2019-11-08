package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.dto.NewFriendNotify;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
public interface IMessageService {


    void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO);

    void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO);

    void sendGroupNotify(String destId, String gid, List<GroupMsgRecord> notifyList);

    void sendGroupNotify(Set<Object> destIdList, String gid, List<GroupMsgRecord> notifyList);

    void sendGroupNotify(Set<Object> destIdList, String gid, GroupMsgRecord notify);

    void sendGroupMessage(Integer eventCode,Set<Object> destIdList,String gid, List<MsgRecord> msgRecordList);

    void sendGroupMessage(Integer eventCode,Set<Object> destIdList,String gid, MsgRecord msgRecord);

    void sendNotify(Integer eventCode, String destId, Object notify);
}
