package com.allen.imsystem.common.utils;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;

import java.util.HashMap;
import java.util.Map;

public class ParamValidator {

    // 存储参数错误信息
    private Map<String, String> parameters = new HashMap<>();
    // 多条错误信息时，存储最后一条
    private String lastErrMsg;

    public void doValidate(){
        if(parameters.size() > 0){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, parameters, lastErrMsg);
        }
    }

    public boolean notNull(Object parameter){
        return parameter!=null;
    }

    public boolean equalsTo(Object parameterA,Object parameterB){
        return notNull(parameterA) && notNull(parameterB) && parameterA.equals(parameterB);
    }

    public void paramNotEmpty(String parameter, String paramName,String errMsg){
        if(StringUtil.isEmpty(parameter)){
            this.lastErrMsg = errMsg;
            this.parameters.put("paramName",errMsg);
        }
    }

    /**
     * 密码验证，6-12位，至少包含一个大写字母，一个小写字母，和数字
     * @param password
     * @param errMsg
     */
    public void validatePassword(String password, String errMsg){
        if(StringUtil.isEmpty(password) ||
                !StringUtil.machesPattern(password,"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,12}$")){
            this.lastErrMsg = errMsg;
            this.parameters.put("password",errMsg);
        }
    }

    /**
     * 邮箱格式验证
     * @param email
     * @param errMsg
     */
    public void validateEmail(String email, String errMsg){
        if(StringUtil.isEmpty(email) || !StringUtil.machesPattern(email, "^[\\w]+@[a-zA-z\\d]+\\.[a-zA-z0-9]+$")){
            this.lastErrMsg = errMsg;
            this.parameters.put("email",errMsg);
        }
    }

    /**
     * 用户名格式验证
     */
    public void validateUsername(String username, String errMsg){
        if(StringUtil.isEmpty(username) || StringUtil.lengthNotBetween(username,1,12)){
            this.lastErrMsg = errMsg;
            this.parameters.put("username",errMsg);
        }
    }


}
