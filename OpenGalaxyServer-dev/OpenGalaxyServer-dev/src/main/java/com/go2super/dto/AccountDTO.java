package com.go2super.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AccountDTO {

    @Email
    @NotNull
    private String email;

    @NotNull
    @Size(min = 3, max = 18)
    private String username;

    @NotNull
    @Size(min = 4, max = 26)
    private String password;

    private String captcha;
    private String otp;

}
