package com.allen.imsystem.user.model.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class EditUserInfoParam {

    private String uid;

    private String email;

    @NotEmpty
    @Length(min = 1, max = 12, message = "用户名格式错误")
    private String username;

    @NotEmpty
    @Length(min = 1, max = 30, message = "个性签名长度应在1到30个字符")
    private String signWord;

    private Integer gender;

    private Integer age;

    private String avatar;
}
