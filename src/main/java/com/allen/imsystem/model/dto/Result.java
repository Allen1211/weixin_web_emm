package com.allen.imsystem.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    private boolean isSuccess;
    private ErrMsg errMsg;

    public Result() {
    }

    public Result(boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.errMsg = null;
    }

    public Result(boolean isSuccess, ErrMsg errMsg) {
        this.isSuccess = isSuccess;
        this.errMsg = errMsg;
    }
}
