package com.allen.imsystem.model.pojo;

import lombok.Data;

@Data
public class UidPool {
    private Integer id;
    private String uid;
    private boolean has_used;
}
