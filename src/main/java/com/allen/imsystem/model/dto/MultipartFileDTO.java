package com.allen.imsystem.model.dto;

import lombok.Data;
import org.apache.commons.fileupload.FileItem;

import java.util.HashMap;

@Data
public class MultipartFileDTO {
    //该请求是否是multipart
    private boolean isMultipart;
    //任务ID
    private String taskId;
    //总分片数量
    private int blockNum;
    //当前为第几块分片
    private int currBlock;
    //当前分片大小
    private long currBlockSize = 0L;
    //文件名
    private String fileName;
    //分片对象
    private FileItem fileItem;
    //请求中附带的自定义参数
    private HashMap<String, String> param = new HashMap<>();
}
