package com.linked.classbridge.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OpenSearchConfig {

    @Value("${AWS_ES_ACCESS_KEY}")
    private String accessKey;

    @Value("${AWS_ES_SECRET_KEY}")
    private String secretKey;

    @Bean
    public AWSCredentialsProvider customCredentialsProvider() {
        log.info("Using AWS credentials for access.");
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }


}
