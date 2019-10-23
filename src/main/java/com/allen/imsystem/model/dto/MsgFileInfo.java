package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class MsgFileInfo {
    private String fileName;
    private String downloadUrl;

    public MsgFileInfo() {
    }

    public MsgFileInfo(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }
}
