package com.allen.imsystem.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class JSONResponse {
    private Integer status;
    private Integer code;
    private Object errMsg;
    private Map<String,Object> data;

    public JSONResponse() {
    }

    public JSONResponse(Integer status) {
        this.status = status;
    }

    public JSONResponse(Integer status,Integer code) {
        this.code = code;
        this.status = status;
    }

    public JSONResponse(Integer status, Map<String,Object> data) {
        this(status);
        this.data = data;
    }
    public JSONResponse(Integer status, Integer code, Object errMsg) {
        this(status,code);
        this.errMsg = errMsg;
    }

    public JSONResponse(Integer status, Integer code, Object errMsg, Map<String,Object> data) {
        this(status,code,errMsg);
        this.data = data;
    }

    public JSONResponse success(){
        this.status = 1;
        return this;
    }

    public JSONResponse fail(){
        this.status = 0;
        return this;
    }

    public JSONResponse setCode(Integer code){
        this.code = code;
        return this;
    }

    public JSONResponse putData(String key, Object value){
        if(data == null){
            data = new HashMap<>();
        }
        data.put(key,value);
        return this;
    }

    public JSONResponse putAllData(Map dataMap){
        if(data == null){
            data = new HashMap<>();
        }
        data.putAll(dataMap);
        return this;
    }
}
