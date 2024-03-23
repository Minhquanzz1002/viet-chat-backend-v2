package vn.edu.iuh.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    private ObjectMetadata getMetaData(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());
        return objectMetadata;
    }
}
