package com.allen.imsystem.message.service.impl;


import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class NotifyPackage {
    private Integer notifyType;
    private Set<Object> destIdSet;
    private List notifyContentList;

    private NotifyPackage(Integer notifyType, Set<Object> destIdSet, List notifyContentList) {
        this.notifyType = notifyType;
        this.destIdSet = destIdSet;
        this.notifyContentList = notifyContentList;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private Integer notifyType;
        private Set<Object> destIdSet;
        private List notifyContentList;

        public Builder() {
            destIdSet = new HashSet<>(16);
            notifyContentList = new ArrayList<>(16);
        }

        public Builder(int receiverNum) {
            destIdSet = new HashSet<>(receiverNum);
        }

        public Builder type(Integer notifyType){
            this.notifyType = notifyType;
            return this;
        }

        public Builder receiver(Object destId){
            if(destId == null){
                throw new NullPointerException("destId cannot be null");
            }
            destIdSet.add(destId);
            return this;
        }

        public Builder receivers(Set<Object> receivers){
            if(receivers == null){
                throw new NullPointerException("receiverSet cannot be null");
            }
            destIdSet.addAll(receivers);
            return this;
        }

        public Builder notifyContent(Object content){
            if(content == null){
                throw new NullPointerException("content cannot be null");
            }
            notifyContentList.add(content);
            return this;
        }

        public Builder notifyContents(List contentList){
            if(contentList == null){
                throw new NullPointerException("content cannot be null");
            }
            notifyContentList = contentList;
            return this;
        }

        public NotifyPackage build(){
            return new NotifyPackage(notifyType,destIdSet, notifyContentList);
        }

    }
}
