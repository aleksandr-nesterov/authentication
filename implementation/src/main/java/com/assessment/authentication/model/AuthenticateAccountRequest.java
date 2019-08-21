package com.assessment.authentication.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateAccountRequest {

    private String username;
    private String password;
}
