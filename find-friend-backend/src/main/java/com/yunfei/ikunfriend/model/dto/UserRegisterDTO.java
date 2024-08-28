package com.yunfei.ikunfriend.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String ikunCode;
}
