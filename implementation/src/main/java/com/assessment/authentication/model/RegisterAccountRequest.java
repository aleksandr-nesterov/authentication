package com.assessment.authentication.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterAccountRequest {

    private String accountNumber;
    private String username;
    private String password;
}
