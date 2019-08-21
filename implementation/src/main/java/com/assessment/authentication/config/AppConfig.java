package com.assessment.authentication.config;

import com.assessment.authentication.properties.AccountApiProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class AppConfig {

    @Autowired
    AccountApiProperties accountApiProperties;

    @Bean
    RestTemplate restTemplate() throws Exception {
        ClassPathResource keyStore = new ClassPathResource(accountApiProperties.getKeyStore());
        ClassPathResource trustStore = new ClassPathResource(accountApiProperties.getTrustStore());

        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(keyStore.getURL(),
                        accountApiProperties.getKeyStorePassword().toCharArray(),
                        accountApiProperties.getKeyStorePassword().toCharArray())
                .loadTrustMaterial(trustStore.getURL(), accountApiProperties.getTrustStorePassword().toCharArray())
                .build();

        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
        return new RestTemplate(httpComponentsClientHttpRequestFactory);

    }

}
