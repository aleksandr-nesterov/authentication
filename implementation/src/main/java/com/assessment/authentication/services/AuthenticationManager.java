package com.assessment.authentication.services;

import com.assessment.authentication.exceptions.AuthenticationException;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class AuthenticationManager {

    private static final int MAX_NUMBER_OF_FAILURES = 3;

    private final byte[] passwordChecksum;
    private int failedAttemptsCounter;
    private ZonedDateTime blockageDate;

    public AuthenticationManager(String password) {
        this(password, null);
    }

    // visible for testing
    AuthenticationManager(String password, ZonedDateTime blockageDate) {
        this.passwordChecksum = MessageDigestHolder.MESSAGE_DIGEST.digest(password.getBytes());
        this.blockageDate = blockageDate;
    }

    public synchronized boolean authenticate(String otherPassword) {
        if (isBlocked()) {
            throw new AuthenticationException("Account is blocked for 24 hours");
        }

        byte[] otherPasswordChecksum = MessageDigestHolder.MESSAGE_DIGEST.digest(otherPassword.getBytes());

        if (Arrays.equals(passwordChecksum, otherPasswordChecksum)) {
            failedAttemptsCounter = 0;
            return true;
        }

        if (++failedAttemptsCounter == MAX_NUMBER_OF_FAILURES) {
            blockageDate = ZonedDateTime.now(ZoneOffset.UTC);
            failedAttemptsCounter = 0;
        }

        return false;

    }

    synchronized int getFailedAttemptsCounter() {
        return failedAttemptsCounter;
    }

    // visible for testing
    synchronized boolean isBlocked() {
        return blockageDate != null && blockageDate.plusDays(1).isAfter(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Slf4j
    private static final class MessageDigestHolder {

        private static final MessageDigest MESSAGE_DIGEST = initMessageDigest();

        private static MessageDigest initMessageDigest() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }

    }
}
