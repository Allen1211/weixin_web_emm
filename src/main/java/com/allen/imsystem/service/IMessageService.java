package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.SendMsgDTO;
import org.springframework.stereotype.Service;

@Service
public interface IMessageService {


    void sendPrivateMessage(SendMsgDTO sendMsgDTO);
}
