package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.id.IdPoolService;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用于批量生成群通知的工厂
 */
public class GroupNotifyFactory {
    private GroupMsgRecord baseNotify;
    private List<GroupMsgRecord> notifyList;

    private IdPoolService idPoolService;

    private GroupNotifyFactory(String gid) {
        this.baseNotify = new GroupMsgRecord(0L,gid,gid,
                4,"","",true,new Date(),new Date());
        this.notifyList = new ArrayList<>();
        this.idPoolService = BeanUtil.getBean(IdPoolService.class);
    }

    public static GroupNotifyFactory getInstance(String gid){
        return new GroupNotifyFactory(gid);
    }

    public GroupNotifyFactory appendNotify(String content){
        try {
            GroupMsgRecord notify = (GroupMsgRecord) BeanUtils.cloneBean(baseNotify);
            notify.setMsgId(idPoolService.nextMsgId());
            notify.setContent(content);
            notifyList.add(notify);
            return this;
        }catch(Exception e){
            e.printStackTrace();
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    public List<GroupMsgRecord> done(){
        return this.notifyList;
    }
}
