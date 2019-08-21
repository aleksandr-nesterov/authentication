package com.assessment.authentication.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("account-api")
public class AccountApiProperties {

    private String baseUrl;
    private String keyStore;
    private String keyStorePassword;
    private String trustStore;
    private String trustStorePassword;

}
