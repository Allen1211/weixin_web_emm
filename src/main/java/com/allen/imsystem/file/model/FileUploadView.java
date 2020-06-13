package com.allen.imsystem.file.model;

import lombok.Data;

import java.util.List;

@Data
public class FileUploadView {
    private Integer uploadType;
    private String downloadUrl;
    private List<Integer> blockList;
    private Integer blockNum;

    public FileUploadView() {
    }

    public FileUploadView(Integer uploadType) {
        this.uploadType = uploadType;
    }

    public FileUploadView(Integer uploadType, String downloadUrl) {
        this.uploadType = uploadType;
        this.downloadUrl = downloadUrl;
    }

    public FileUploadView(Integer uploadType, List<Integer> blockList, Integer blockNum) {
        this.uploadType = uploadType;
        this.blockList = blockList;
        this.blockNum = blockNum;
    }
}
