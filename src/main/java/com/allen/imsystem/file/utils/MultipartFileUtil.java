package com.allen.imsystem.file.utils;

import com.allen.imsystem.file.model.MultipartFileDTO;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MultipartFileUtil {

    /**
     * 在HttpServletRequest中获取分段上传文件请求的信息
     *
     * @param request
     * @return
     */
    public static MultipartFileDTO parse(HttpServletRequest request) throws Exception {
        MultipartFileDTO param = new MultipartFileDTO();

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        param.setMultipart(isMultipart);
        if (isMultipart) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 得到所有的表单域，它们目前都被当作FileItem
            List<FileItem> fileItems = upload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                if (fileItem.getFieldName().equals("md5")) {
                    param.setMd5(fileItem.getString());
                } else if (fileItem.getFieldName().equals("fileName")) {
                    param.setFileName(new String(fileItem.getString().getBytes(
                            StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                } else if (fileItem.getFieldName().equals("blockNum")) {
                    param.setBlockNum(NumberUtils.toInt(fileItem.getString()));
                } else if (fileItem.getFieldName().equals("currBlock")) {
                    param.setCurrBlock(NumberUtils.toInt(fileItem.getString()));
                } else if (fileItem.getFieldName().equals("fileData")) {
                    param.setFileItem(fileItem);
                } else if (fileItem.getFieldName().equals("totalSize")) {
                    param.setTotalSize(Long.parseLong(fileItem.getString()));
                } else {
                    param.getParam().put(fileItem.getFieldName(), fileItem.getString());
                }
            }
        }

        return param;
    }


}