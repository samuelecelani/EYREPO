package it.ey.piao.bff.configuration;

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
//Configurazione di un bucket S3
@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
        @Value("${aws.s3.endpoint:test}") String s3Endpoint,
        @Value("${aws.credentials.access-key:test}") String accessKey,
        @Value("${aws.credentials.secret-key:test}") String secretKey,
        @Value("${aws.s3.region:eu-west-1}") String region
    ) {
        try {
            if (s3Endpoint == null || accessKey == null || secretKey == null || region == null) {
                throw new IllegalArgumentException("Missing required AWS S3 configuration properties");
            }

            System.out.println("[S3Config] Creating S3Client with:");
            System.out.println(" - Endpoint: " + s3Endpoint);
            System.out.println(" - Region: " + region);
            System.out.println(" - AccessKey: " + accessKey);
            System.out.println(" - SecretKey: " + "*".repeat(secretKey.length()));

            return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                    .putAdvancedOption(SdkAdvancedClientOption.SIGNER, AwsS3V4Signer.create())
                    .build())
                .serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .checksumValidationEnabled(false)
                    .chunkedEncodingEnabled(false)
                    .build())
                .build();
        } catch (Exception e) {
            System.err.println("[S3Config] Error creating S3Client: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Bean
    public S3Presigner s3Presigner(
        @Value("${aws.s3.endpoint}") String s3Endpoint,
        @Value("${aws.credentials.access-key}") String accessKey,
        @Value("${aws.credentials.secret-key}") String secretKey,
        @Value("${aws.s3.region}") String region
    ) {
        return S3Presigner.builder()
            .endpointOverride(URI.create(s3Endpoint)) // importante per MinIO
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))

            .region(Region.of(region))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())
            .build();

    }

}

