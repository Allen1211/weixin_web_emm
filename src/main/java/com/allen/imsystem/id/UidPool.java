package com.allen.imsystem.id;

import lombok.Data;

@Data
public class UidPool {
    private Integer id;
    private String uid;
    private boolean has_used;
}
