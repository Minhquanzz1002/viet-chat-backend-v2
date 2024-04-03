package vn.edu.iuh.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.dto.enums.UploadType;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    @Value("${aws.bucket.name}")
    private String bucketName;
    @Value("${aws.endpoint-url}")
    private String endpointUrl;
    private final AmazonS3 s3Client;

    public String uploadFile(MultipartFile multipartFile, String filename, String type) {
        try {
            String url = type + "/" + filename;
            s3Client.putObject(bucketName, url, multipartFile.getInputStream(), getMetaData(multipartFile));
            return endpointUrl + url;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new AmazonClientException("Error occurred while uploading to s3");
        }
    }

    @Async
    public String save(String filename, UploadType type, String id) {
        String newFilename;
        if (type == UploadType.MESSAGE) {
            newFilename = type.getLink() + UUID.randomUUID() + "/" + filename;
        } else if (type == UploadType.AVATAR) {
            newFilename = type.getLink() + id + filename.substring(filename.lastIndexOf("."));
        } else {
            newFilename = type.getLink() + UUID.randomUUID() + filename.substring(filename.lastIndexOf("."));
        }
        return generateUrl(newFilename, HttpMethod.PUT);
    }

    @Async
    public String findByName(String filename) {
        if (!s3Client.doesObjectExist(bucketName, filename)) {
            return "File does not exist";
        }
        return generateUrl(filename, HttpMethod.GET);
    }

    private String generateUrl(String filename, HttpMethod httpMethod) {
        Instant expirationTime = Instant.now().plus(Duration.ofHours(24));
        return s3Client.generatePresignedUrl(bucketName, filename, Date.from(expirationTime), httpMethod).toString();
    }

    private ObjectMetadata getMetaData(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }
}
