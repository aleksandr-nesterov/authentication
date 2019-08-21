package com.assessment.authentication.controllers;

import com.assessment.authentication.exceptions.AuthenticationException;
import com.assessment.authentication.model.AuthenticateAccountRequest;
import com.assessment.authentication.model.AuthenticateAccountResponse;
import com.assessment.authentication.model.RegisterAccountRequest;
import com.assessment.authentication.properties.AccountApiProperties;
import com.assessment.authentication.services.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AuthenticationController {

    private final RestTemplate restTemplate;
    private final AccountApiProperties accountApiProperties;
    private final AuthenticationService authenticationService;

    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity registerAccount(@RequestBody RegisterAccountRequest registerAccount) {

        validatePassword(registerAccount.getPassword());

        if (hasBankAccount(registerAccount)) {
            authenticationService.register(registerAccount.getUsername(), registerAccount.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        throw new AuthenticationException("Bank account does not exist");
    }

    @PostMapping("/authenticate")
    public ResponseEntity authenticateAccount(@RequestBody AuthenticateAccountRequest request) {

        AuthenticateAccountResponse response = authenticationService.authenticate(request.getUsername(), request.getPassword());

        return ResponseEntity.ok().body(response);
    }

    private void validatePassword(String password) {

        if (password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters long");
        }
    }

    private boolean hasBankAccount(RegisterAccountRequest registerAccount) {
        String accountApiUrl = accountApiProperties.getBaseUrl() + registerAccount.getAccountNumber();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(accountApiUrl, String.class);
            log.debug(response.toString());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }
}
