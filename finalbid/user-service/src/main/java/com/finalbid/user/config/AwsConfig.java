package com.finalbid.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * AWS S3 client configuration.
 * When AWS_ENDPOINT_OVERRIDE is set (local with LocalStack), overrides the endpoint.
 * When unset or empty (AWS profile), uses the real S3 endpoint.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:eu-central-1}")
    private String region;

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Value("${AWS_ACCESS_KEY_ID:test}")
    private String accessKeyId;

    @Value("${AWS_SECRET_ACCESS_KEY:test}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ));

        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride))
                   .forcePathStyle(true);   // required for LocalStack
        }

        return builder.build();
    }
}
