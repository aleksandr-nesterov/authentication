package com.assessment.authentication.integration;

import com.assessment.authentication.model.AuthenticateAccountRequest;
import com.assessment.authentication.services.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationApiIntegrationTest {

    private static final String ACCOUNT_NUMBER = "77853449";
    private static final String USERNAME = "test-username";
    private static final String PASSWORD = "test-password";

    private static final String TOKEN = "123456789abcd";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RestTemplate restTemplate;
    @MockBean
    JwtTokenProvider jwtTokenProviderMock;

    MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void expect200OnAccountRegistration() throws Exception {
        mockAccountApi(ACCOUNT_NUMBER, HttpStatus.OK);

        performSuccessfullRegistration(ACCOUNT_NUMBER, USERNAME, PASSWORD);

    }

    @Test
    void expect400OnInvalidBankAccount() throws Exception {
        mockAccountApi("invalid-bank-account", HttpStatus.NOT_FOUND);

        RegisterAccount registerAccount = new RegisterAccount();
        registerAccount.setAccountNumber("invalid-bank-account");
        registerAccount.setUsername(USERNAME);
        registerAccount.setPassword(PASSWORD);

        mockMvc.perform(post("/api/v1/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bank account does not exist"));

    }

    @Test
    void expect400OnInvalidPassword() throws Exception {

        RegisterAccount registerAccount = new RegisterAccount();
        registerAccount.setAccountNumber(ACCOUNT_NUMBER);
        registerAccount.setUsername(USERNAME);
        registerAccount.setPassword("12345");

        mockMvc.perform(post("/api/v1/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerAccount)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be at least 6 characters long"));

    }

    @Test
    void expect200OnValidAuthentication() throws Exception {
        mockAccountApi(ACCOUNT_NUMBER, HttpStatus.OK);

        performSuccessfullRegistration(ACCOUNT_NUMBER, USERNAME, PASSWORD);

        performSuccessfulAuthentication();

    }

    @Test
    void expect400When3AuthenticationFailures() throws Exception {
        mockAccountApi(ACCOUNT_NUMBER, HttpStatus.OK);

        performSuccessfullRegistration(ACCOUNT_NUMBER, USERNAME, PASSWORD);

        String message = "Invalid password";
        performAuthenticationFailure(message);
        performAuthenticationFailure(message);
        performAuthenticationFailure(message);

        message = "Account is blocked for 24 hours";
        performAuthenticationFailure(message);

    }

    private void performAuthenticationFailure(String message) throws Exception {
        AuthenticateAccountRequest request = new AuthenticateAccountRequest();
        request.setUsername(USERNAME);
        request.setPassword("invalid-password");

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(message));
    }

    private void performSuccessfulAuthentication() throws Exception {

        given(jwtTokenProviderMock.generateToken(USERNAME)).willReturn(TOKEN);

        AuthAccountResponse expectedResponse = new AuthAccountResponse(TOKEN);

        AuthenticateAccountRequest request = new AuthenticateAccountRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        mockMvc.perform(post("/api/v1/authenticate")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
    }

    private void performSuccessfullRegistration(String acccountNumber, String username, String password) throws Exception {
        RegisterAccount registerAccount = new RegisterAccount();
        registerAccount.setAccountNumber(acccountNumber);
        registerAccount.setUsername(username);
        registerAccount.setPassword(password);


        mockMvc.perform(post("/api/v1/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerAccount)))
                .andExpect(status().isCreated());
    }

    private void mockAccountApi(String accountNumber, HttpStatus status) {
        mockServer.expect(once(), requestTo("https://localhost:8444/accounts/" + accountNumber))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON));

    }


    @Getter
    @Setter
    private static class RegisterAccount {

        private String accountNumber;
        private String username;
        private String password;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class AuthAccountResponse {

        private String token;
    }
}
