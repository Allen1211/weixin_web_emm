package com.allen.imsystem.file.model;

import lombok.Data;

import java.util.Date;

@Data
public class FileMd5 {
    private Integer id;
    private String md5;
    private String fileName;
    private Long size;
    private String url;
    private Boolean status = true;
    private Date createdTime;

    public FileMd5() {
    }

    public FileMd5(String md5, String fileName, Long size, String url) {
        this.md5 = md5;
        this.fileName = fileName;
        this.size = size;
        this.url = url;
    }
}
