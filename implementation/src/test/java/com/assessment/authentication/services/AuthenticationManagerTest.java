package com.assessment.authentication.services;

import com.assessment.authentication.exceptions.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.assessment.authentication.services.AuthenticationManager.Result.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationManagerTest {

    private static final String PASSWORD = "your_password";
    private static final String WRONG_PASSWORD = "wrong-password";

    @Test
    void expectSuccessfulAuth() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        assertEquals(SUCCESS, authenticationManager.authenticate(PASSWORD));

    }

    @Test
    void expectFailedAuth() {
        AuthenticationManager authenticationManager = new AuthenticationManager(PASSWORD);

        assertEquals(INVALID_PASSWORD, authenticationManager.authenticate(WRONG_PASSWORD));

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

        AuthenticationManager.Result result = authenticationManager.authenticate(WRONG_PASSWORD);

        assertEquals(BLOCKED, result);

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
