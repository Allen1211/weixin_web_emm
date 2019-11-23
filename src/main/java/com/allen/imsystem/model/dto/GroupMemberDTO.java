package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class GroupMemberDTO {
    private String uid;
    private String groupAlias;
    private String username;
    private String avatar;
    private String signWord;
    private Integer relation;
}
