package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class MultiFileResponse {
    private String taskId;
    private Integer currBlock;
    private Boolean isComplete;
    private String downloadUrl;
}
