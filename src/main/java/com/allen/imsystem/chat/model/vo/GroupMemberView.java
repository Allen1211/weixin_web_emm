package com.allen.imsystem.chat.model.vo;

import lombok.Data;

@Data
public class GroupMemberView {
    private String uid;
    private String groupAlias;
    private String username;
    private String avatar;
    private String signWord;
    private Integer relation;
}
