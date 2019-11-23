package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseMsgRecord implements Serializable, Cloneable {
    protected Long msgId;
    protected Integer msgType;
    protected String content;
    protected String fileMd5;
    protected Boolean status;
    protected Date createdTime;
    protected Date updateTime;
}
