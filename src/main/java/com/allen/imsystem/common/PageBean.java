package com.allen.imsystem.common;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import lombok.Data;

/**
 * 用于查询分页
 */
@Data
public class PageBean {
    private Integer from;
    private Integer offset;



    public PageBean(Integer index, Integer pageSize){
        if(pageSize == null || pageSize == 0) pageSize = 10;
        if(index<=0 || pageSize <= 0){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"不合法的分页参数");
        }
        from = (index-1)*pageSize;
        offset = pageSize;
    }

    public PageBean(Integer index){
        this(index,10);
    }

}
