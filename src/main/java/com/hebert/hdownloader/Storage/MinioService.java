package com.hebert.hdownloader.Storage;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;

@Component
public class MinioService {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioService(
            @Value("${app.minio.endpoint}") String endpoint,
            @Value("${app.minio.accessKey}") String accessKey,
            @Value("${app.minio.secretKey}") String secretKey,
            @Value("${app.minio.bucket}") String bucket) {

        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    public void uploadMusic(String filePath, String fileName) throws MinioException, InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                System.out.println("Bucket does not exist, creating bucket....");
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucket)
                                .build());
            }
        } catch (Exception e) {
            throw new MinioException("Bucket creation/validation error: " + e.getMessage());
        }

        System.out.println("Uploading....");

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object("file/" + fileName)
                        .filename(filePath)
                        .contentType("audio/mpeg")
                        .build()
        );

        System.out.println("File uploaded: " + fileName);

    }

}
