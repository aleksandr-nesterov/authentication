package com.assessment.authentication.services;

import com.assessment.authentication.exceptions.AuthenticationException;
import com.assessment.authentication.model.AuthenticateAccountResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    private static final String USERNAME = "test-username";
    private static final String PASSWORD = "test-password";

    @Mock
    JwtTokenProvider jwtTokenProviderMock;
    @InjectMocks
    AuthenticationService authenticationService;

    @Test
    void expectSuccessfulRegistration() {
        authenticationService.register(USERNAME, PASSWORD);
    }

    @Test
    void expectExceptionOnRegistrationWhenUsernameExists() {
        authenticationService.register(USERNAME, PASSWORD);

        assertThrows(AuthenticationException.class, () -> authenticationService.register(USERNAME, PASSWORD));
    }

    @Test
    void expectExceptionOnAuthenticationWhenUsernameDoesNotExist() {

        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, PASSWORD));
    }

    @Test
    void expectExceptionOnInvalidAuthentication() {
        authenticationService.register(USERNAME, PASSWORD);

        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, "invalid-password"));
    }

    @Test
    void expectValidAuthentication() {
        authenticationService.register(USERNAME, PASSWORD);

        when(jwtTokenProviderMock.generateToken(anyString())).thenReturn("token");

        AuthenticateAccountResponse response = authenticationService.authenticate(USERNAME, PASSWORD);

        assertEquals("token", response.getToken());
    }

    @Test
    void expectAccountBlockageOn3Failures() {
        authenticationService.register(USERNAME, PASSWORD);

        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, "invalid-password"));
        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, "invalid-password"));
        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, "invalid-password"));

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(USERNAME, "invalid-password"));

        assertEquals(ex.getMessage(), "Account is blocked for 24 hours");

    }
}
