package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileUploadInfo {
    private Integer uploadType;
    private String downloadUrl;
    private List<Integer> blockList;
    private Integer blockNum;

    public FileUploadInfo() {
    }

    public FileUploadInfo(Integer uploadType) {
        this.uploadType = uploadType;
    }

    public FileUploadInfo(Integer uploadType, String downloadUrl) {
        this.uploadType = uploadType;
        this.downloadUrl = downloadUrl;
    }

    public FileUploadInfo(Integer uploadType, List<Integer> blockList, Integer blockNum) {
        this.uploadType = uploadType;
        this.blockList = blockList;
        this.blockNum = blockNum;
    }
}
