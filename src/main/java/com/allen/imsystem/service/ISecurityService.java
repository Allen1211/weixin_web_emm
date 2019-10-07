package com.allen.imsystem.service;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import org.springframework.stereotype.Service;

@Service
public interface ISecurityService {

    boolean verifyImageCode(String imageCode, String correctImageCode);

    boolean verifyEmailCode(String emailCode, String emailCodeToken);

}
