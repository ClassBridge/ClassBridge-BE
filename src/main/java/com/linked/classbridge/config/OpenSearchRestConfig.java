package com.linked.classbridge.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.linked.classbridge.repository") // repo 경로로 바꿔주기
@Slf4j
public class OpenSearchRestConfig extends AbstractOpenSearchConfiguration {

    @Value("${ELASTICSEARCH_ENDPOINT}")
    private String endpoint;

    @Value("${ELASTICSEARCH_REGION}")
    private String region;

    private final AWSCredentialsProvider credentialsProvider;

    @Autowired
    public OpenSearchRestConfig(AWSCredentialsProvider provider) {
        credentialsProvider = provider;
    }

    @Override
    @Bean
    public RestHighLevelClient opensearchClient() {
        AWS4Signer signer = new AWS4Signer();
        String serviceName = "es";
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);

        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(endpoint, 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.addInterceptorLast(interceptor))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) Duration.ofSeconds(300).toMillis())
                        .setSocketTimeout((int) Duration.ofSeconds(150).toMillis()));

//        ClientConfiguration configuration = ClientConfiguration.builder()
//                .connectedTo(endpoint)
//                .usingSsl()
//                .withBasicAuth(username, password)
//                .build();

        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        return client;
//        return RestClients.create(configuration).rest();
    }


}

