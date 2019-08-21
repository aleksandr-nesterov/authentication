package com.assessment.authentication.services;

import com.assessment.authentication.exceptions.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationManagerTest {

    private static final String PASSWORD = "your_password";
    private static final String WRONG_PASSWORD = "wrong-password";

    @Test
    void expectSuccessfulAuth() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        assertTrue(authenticationManager.authenticate(PASSWORD));

    }

    @Test
    void expectFailedAuth() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        assertFalse(authenticationManager.authenticate(WRONG_PASSWORD));

    }

    @Test
    void expectCounterToBeZeroAfter3Failures() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        authenticationManager.authenticate(WRONG_PASSWORD);
        assertEquals(1, authenticationManager.getFailedAttemptsCounter());

        authenticationManager.authenticate(WRONG_PASSWORD);
        assertEquals(2, authenticationManager.getFailedAttemptsCounter());

        authenticationManager.authenticate(WRONG_PASSWORD);
        assertEquals(0, authenticationManager.getFailedAttemptsCounter());

    }

    @Test
    void expectCounterToBeZeroAfter2Failures1Success() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        authenticationManager.authenticate(WRONG_PASSWORD);
        assertEquals(1, authenticationManager.getFailedAttemptsCounter());

        authenticationManager.authenticate(WRONG_PASSWORD);
        assertEquals(2, authenticationManager.getFailedAttemptsCounter());

        authenticationManager.authenticate(PASSWORD);
        assertEquals(0, authenticationManager.getFailedAttemptsCounter());

    }

    @Test
    void expectAccountBlockageAfter3Failures() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        authenticationManager.authenticate(WRONG_PASSWORD);
        authenticationManager.authenticate(WRONG_PASSWORD);
        authenticationManager.authenticate(WRONG_PASSWORD);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () -> authenticationManager.authenticate(WRONG_PASSWORD));

        assertEquals(ex.getMessage(), "Account is blocked for 24 hours");

    }

    @Test
    void checkAccountBlockage() {

        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD, ZonedDateTime.now(ZoneOffset.UTC).minusHours(23));

        assertTrue(authenticationManager.isBlocked());
    }

    @Test
    void checkAccountUnBlock() {

        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD, ZonedDateTime.now(ZoneOffset.UTC).minusHours(25));

        assertFalse(authenticationManager.isBlocked());
    }


}
