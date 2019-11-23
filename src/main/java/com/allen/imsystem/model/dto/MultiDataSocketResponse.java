package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MultiDataSocketResponse {
    private Integer eventCode;
    private Integer status;
    private Integer code;
    private ErrMsg errMsg;
    private Map<String,Object> data;

    public MultiDataSocketResponse() {
    }

    public MultiDataSocketResponse(Integer eventCode, Integer status) {
        this.eventCode = eventCode;
        this.status = status;
    }

    public MultiDataSocketResponse(Integer eventCode, Integer status, Integer code) {
        this.eventCode = eventCode;
        this.status = status;
        this.code = code;
    }

    public MultiDataSocketResponse(Integer eventCode, Integer status, Integer code, ErrMsg errMsg) {
        this.eventCode = eventCode;
        this.status = status;
        this.code = code;
        this.errMsg = errMsg;
    }

    public MultiDataSocketResponse putData(String key, Object val){
        if(data == null){
            data = new HashMap<>();
        }
        data.put(key,val);
        return this;
    }
}
