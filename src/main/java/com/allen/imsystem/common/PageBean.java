package com.allen.imsystem.common;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import lombok.Data;

@Data
public class PageBean {
    private Integer from;
    private Integer offset;

    public PageBean(Integer index, Integer pageSize){
        if(index<0 || pageSize <= 0){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"不合法的分页参数");
        }
        if(pageSize == null || pageSize == 0) pageSize = 10;
        from = (index-1)*pageSize;
        offset = pageSize;
    }

    public PageBean(Integer index){
        this(index,10);
    }

    public static void main(String[] args) {
        PageBean pageBean = new PageBean(5,10);
        System.out.println(pageBean);
    }
}
