package com.allen.imsystem.model.dto;

import com.allen.imsystem.common.Const.GlobalConst;
import lombok.Data;

@Data
public class MsgFileInfo {
    private String fileName;
    private String downloadUrl;
    private String fileIcon = GlobalConst.Path.FILE_ICON_URL;
    private String fileSize;

    private Long size;
    public MsgFileInfo() {
    }

    public MsgFileInfo(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }
}
