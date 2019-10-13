package com.allen.imsystem.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class UserInfoDTO {

    private String uid;

    private String username;

    private String signWord;

    private String avatar;


}
