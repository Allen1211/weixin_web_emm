package com.allen.imsystem.common;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupNotifyFactory {
    private GroupMsgRecord baseNotify;
    private List<GroupMsgRecord> notifyList;

    private GroupNotifyFactory(String gid) {
        this.baseNotify = new GroupMsgRecord(0L,gid,gid,
                4,"","",true,new Date(),new Date());
        this.notifyList = new ArrayList<>();
    }

    public static GroupNotifyFactory getInstance(String gid){
        return new GroupNotifyFactory(gid);
    }

    public GroupNotifyFactory appendNotify(String content){
        try {
            GroupMsgRecord notify = (GroupMsgRecord) BeanUtils.cloneBean(baseNotify);
            notify.setMsgId(SnowFlakeUtil.getNextSnowFlakeId());
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
