package com.ija.student_management_portal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration class for AWS S3 client beans
 */
@Configuration
public class S3Config {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(fileStorageConfig.getS3Region()))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(fileStorageConfig.getS3Region()))
                .build();
    }
}
