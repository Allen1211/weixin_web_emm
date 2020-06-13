package com.allen.imsystem.common.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "统一返回封装类")
@Getter
@Setter
public class JSONResponse {
    @ApiModelProperty("请求状态 1->成功 0->失败")
    private Integer status;
    @ApiModelProperty("返回码 1000代表成功 其他请参考文档")
    private Integer code;
    @ApiModelProperty("错误信息 请求不成功时提供错误提示")
    private Object errMsg;
    @ApiModelProperty("返回的数据对象封装")
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
