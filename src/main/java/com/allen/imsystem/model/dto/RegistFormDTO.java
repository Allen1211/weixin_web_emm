package com.allen.imsystem.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public class RegistFormDTO {

    @NotNull(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Length(min = 1, max = 12, message = "用户名格式错误")
    private String username;

//    @Pattern(regexp = GlobalConst.RegExp.PASSWORD, message = "密码格式错误")
    @NotEmpty
    private String password;

    @NotEmpty(message = "验证码不可为空")
    private String code;

    @NotEmpty(message = "邮箱验证码不可为空")
    private String emailCode;




}
