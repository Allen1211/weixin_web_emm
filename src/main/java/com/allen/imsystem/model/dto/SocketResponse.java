package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class SocketResponse {
    private Integer eventCode;
    private Integer status;
    private Integer code;
    private Object errMsg;
    private Object data;

    public SocketResponse(Integer eventCode, Integer status) {
        this.eventCode = eventCode;
        this.status = status;
    }


    public SocketResponse(Integer eventCode, Integer status, Object data) {
        this.eventCode = eventCode;
        this.status = status;
        this.data = data;
    }

    public SocketResponse(Integer eventCode, Integer status, Integer code, Object errMsg, Object data) {
        this.eventCode = eventCode;
        this.status = status;
        this.code = code;
        this.errMsg = errMsg;
        this.data = data;
    }
}
