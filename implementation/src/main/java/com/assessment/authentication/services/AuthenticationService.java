package com.assessment.authentication.services;

import com.assessment.authentication.model.AuthenticateAccountResponse;
import com.assessment.authentication.exceptions.AuthenticationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class AuthenticationService {

    private final Map<String, AuthenticationManager> accountRegistry = new ConcurrentHashMap<>();

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register account with username/password in the registry.
     *
     * @param username account username
     * @param password account password
     */
    public void register(String username, String password) {

        AuthenticationManager authenticationManager = new AuthenticationManager(password);

        if (accountRegistry.putIfAbsent(username, authenticationManager) != null) {
            throw new AuthenticationException("Username is already registered");
        }

    }

    /**
     * Authenticate username and generate JWT token.
     *
     * @param username account username
     * @param password account password
     *
     * @return {@link AuthenticateAccountResponse} response
     */
    public AuthenticateAccountResponse authenticate(String username, String password) {

        AuthenticationManager authenticationManager = accountRegistry.get(username);

        if (authenticationManager == null) {
            throw new AuthenticationException("Account is not registered");
        }

        if (authenticationManager.authenticate(password)) {
            String token = jwtTokenProvider.generateToken(username);
            AuthenticateAccountResponse response = new AuthenticateAccountResponse();
            response.setToken(token);
            return response;
        }

        throw new AuthenticationException("Invalid password");

    }

}
