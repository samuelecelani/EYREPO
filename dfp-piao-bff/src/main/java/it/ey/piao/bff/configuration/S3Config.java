package it.ey.piao.bff.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

import org.springframework.util.StringUtils;

//Configurazione di un bucket S3
@Slf4j
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
        @Value("${aws.s3.endpoint:}") String s3Endpoint,
        @Value("${aws.credentials.access-key:}") String accessKey,
        @Value("${aws.credentials.secret-key:}") String secretKey,
        @Value("${aws.s3.region:eu-west-1}") String region
    ) {
        try {
            if (!StringUtils.hasText(region)) {
                throw new IllegalArgumentException("Missing required AWS S3 region configuration");
            }

            log.info("[S3Config] Creating S3Client with:");
            log.info(" - Endpoint: {}", StringUtils.hasText(s3Endpoint) ? s3Endpoint : "AWS default (resolved from region)");
            log.info(" - Region: {}", region);
            log.info(" - AccessKey: {}", StringUtils.hasText(accessKey) ? accessKey : "using default credentials chain");

            var builder = S3Client.builder()
                .region(Region.of(region));

            // Endpoint custom solo per S3-compatibili (es. MinIO)
            if (StringUtils.hasText(s3Endpoint)) {
                builder.endpointOverride(URI.create(s3Endpoint));
                builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .checksumValidationEnabled(false)
                    .chunkedEncodingEnabled(false)
                    .build());
            }

            // Credenziali esplicite solo se fornite, altrimenti usa la default credentials chain di AWS
            if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
                builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                ));
                builder.overrideConfiguration(ClientOverrideConfiguration.builder()
                    .putAdvancedOption(SdkAdvancedClientOption.SIGNER, AwsS3V4Signer.create())
                    .build());
            }

            return builder.build();
        } catch (Exception e) {
            log.error("[S3Config] Error creating S3Client: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public S3Presigner s3Presigner(
        @Value("${aws.s3.endpoint:}") String s3Endpoint,
        @Value("${aws.credentials.access-key:}") String accessKey,
        @Value("${aws.credentials.secret-key:}") String secretKey,
        @Value("${aws.s3.region:eu-west-1}") String region
    ) {
        var builder = S3Presigner.builder()
            .region(Region.of(region));

        // Endpoint custom solo per S3-compatibili (es. MinIO)
        if (StringUtils.hasText(s3Endpoint)) {
            builder.endpointOverride(URI.create(s3Endpoint));
            builder.serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build());
        }

        // Credenziali esplicite solo se fornite
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ));
        }

        return builder.build();
    }

}
