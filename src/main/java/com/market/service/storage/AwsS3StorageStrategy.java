package com.market.service.storage;

import com.market.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 storage strategy implementation.
 * Stores files in Amazon S3 using the AWS SDK v2.
 */
@Component("awsS3StorageStrategy")
public class AwsS3StorageStrategy implements FileStorageStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3StorageStrategy.class);

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.s3.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.s3.shop-profiles-prefix:shop-profiles}")
    private String shopProfilesPrefix;

    @Value("${aws.s3.items-prefix:items}")
    private String itemsPrefix;

    private S3Client s3Client;

    @PostConstruct
    public void initialize() {
        try {
            s3Client = createS3Client();
            // Test the connection by checking if bucket exists
            if (bucketExists()) {
                logger.info("AWS S3 storage strategy initialized successfully");
            } else {
                logger.error("AWS S3 bucket '{}' does not exist or is not accessible", bucketName);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize AWS S3 storage strategy", e);
        }
    }

    @Override
    public String storeShopProfileImage(MultipartFile multipartFile, Long shopId) throws IOException {
        validateImageFile(multipartFile);

        try {
            // Generate unique filename
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = "profile-" + UUID.randomUUID().toString() + fileExtension;
            String objectKey = shopProfilesPrefix + "/shop-" + shopId + "/" + uniqueFilename;

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    multipartFile.getInputStream(), multipartFile.getSize()));

            logger.info("Stored shop profile image using AWS S3: shopId={}, objectKey={}, filename={}",
                    shopId, objectKey, uniqueFilename);

            // Return storage identifier (object key)
            return objectKey;

        } catch (Exception e) {
            logger.error("Failed to store shop profile image in AWS S3", e);
            throw new IOException("Failed to store file in AWS S3", e);
        }
    }

    @Override
    public String storeItemMedia(MultipartFile multipartFile, Long shopId, Long itemId) throws IOException {
        validateMediaFile(multipartFile);

        try {
            // Generate unique filename
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = "item-" + itemId + "-" + UUID.randomUUID().toString() + fileExtension;
            String objectKey = itemsPrefix + "/shop-" + shopId + "/" + uniqueFilename;

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    multipartFile.getInputStream(), multipartFile.getSize()));

            logger.info("Stored item media using AWS S3: shopId={}, itemId={}, objectKey={}, filename={}",
                    shopId, itemId, objectKey, uniqueFilename);

            // Return storage identifier (object key)
            return objectKey;

        } catch (Exception e) {
            logger.error("Failed to store item media in AWS S3", e);
            throw new IOException("Failed to store file in AWS S3", e);
        }
    }

    @Override
    public void deleteFile(String objectKey) throws IOException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Deleted file from AWS S3: objectKey={}", objectKey);
        } catch (Exception e) {
            logger.error("Failed to delete file from AWS S3: objectKey={}", objectKey, e);
            throw new IOException("Failed to delete file from AWS S3", e);
        }
    }

    @Override
    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking if file exists in AWS S3: objectKey={}", objectKey, e);
            return false;
        }
    }

    @Override
    public Resource getFileResource(String objectKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.transferTo(outputStream);

            byte[] fileContent = outputStream.toByteArray();
            return new ByteArrayResource(fileContent);

        } catch (Exception e) {
            logger.error("Failed to get file from AWS S3: objectKey={}", objectKey, e);
            throw new IOException("Failed to get file from AWS S3", e);
        }
    }

    @Override
    public long getFileSize(String storageIdentifier) throws IOException {
        if (!storageIdentifier.startsWith("s3:")) {
            throw new IOException("Invalid AWS S3 storage identifier: " + storageIdentifier);
        }

        String objectKey = storageIdentifier.substring(3);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (Exception e) {
            logger.error("Failed to get file size from AWS S3: objectKey={}", objectKey, e);
            throw new IOException("Failed to get file size from AWS S3", e);
        }
    }

    @Override
    public byte[] readFileChunk(String storageIdentifier, long start, long end) throws IOException {
        if (!storageIdentifier.startsWith("s3:")) {
            throw new IOException("Invalid AWS S3 storage identifier: " + storageIdentifier);
        }

        String objectKey = storageIdentifier.substring(3);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .range("bytes=" + start + "-" + end)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.transferTo(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Failed to read file chunk from AWS S3: objectKey={}", objectKey, e);
            throw new IOException("Failed to read file chunk from AWS S3", e);
        }
    }

    @Override
    public boolean supportsStreaming(String storageIdentifier) {
        // AWS S3 supports efficient range requests for streaming
        return true;
    }

    @Override
    public int getOptimalChunkSize(String storageIdentifier) {
        // AWS S3 supports efficient streaming, so we can use smaller chunks
        return 512 * 1024; // 512KB
    }

    @Override
    public MediaType determineMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MediaType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return MediaType.VIDEO;
            }
        }
        return MediaType.IMAGE; // Default to image
    }

    @Override
    public String getStrategyName() {
        return "aws-s3";
    }

    @Override
    public boolean isAvailable() {
        return s3Client != null && !bucketName.isEmpty() && !accessKeyId.isEmpty() && !secretAccessKey.isEmpty();
    }

    private S3Client createS3Client() {
        if (accessKeyId == null || accessKeyId.trim().isEmpty() || 
            secretAccessKey == null || secretAccessKey.trim().isEmpty()) {
            throw new RuntimeException("AWS S3 credentials are not configured. Please set 'aws.s3.access-key-id' and 'aws.s3.secret-access-key' in application.properties");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    private boolean bucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            logger.error("AWS S3 bucket '{}' does not exist", bucketName);
            return false;
        } catch (Exception e) {
            logger.error("Error checking if AWS S3 bucket exists: {}", bucketName, e);
            return false;
        }
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }

        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IOException("File size must be less than 5MB");
        }

        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IOException("Only JPG, JPEG, PNG, GIF, and WebP files are allowed");
        }
    }

    private void validateMediaFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new IOException("File must be an image or video");
        }

        // Check file size (max 50MB for videos, 5MB for images)
        long maxSize = contentType.startsWith("video/") ? 50 * 1024 * 1024 : 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            String sizeLimit = contentType.startsWith("video/") ? "50MB" : "5MB";
            throw new IOException("File size must be less than " + sizeLimit);
        }

        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (contentType.startsWith("image/")) {
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                throw new IOException("Only JPG, JPEG, PNG, GIF, and WebP image files are allowed");
            }
        } else if (contentType.startsWith("video/")) {
            if (!extension.matches("\\.(mp4|avi|mov|wmv|flv|webm|mkv)")) {
                throw new IOException("Only MP4, AVI, MOV, WMV, FLV, WebM, and MKV video files are allowed");
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
