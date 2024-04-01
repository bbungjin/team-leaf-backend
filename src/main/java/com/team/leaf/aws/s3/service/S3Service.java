package com.team.leaf.aws.s3.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImageToS3(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();

        if (!isValidImage(file)) {
            throw new RuntimeException("이미지 파일이 올바르지 않습니다.");
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getInputStream().available());
        amazonS3Client.putObject(bucket, uuid, file.getInputStream(), metadata);

        return amazonS3Client.getUrl(bucket, uuid).toString();
    }

    public boolean isValidImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (extension.equals("jpeg") || extension.equals("png") ||
                extension.equals("gif") || extension.equals("jpg")) {
            return true;
        }

        return false;
    }

    public void deleteFile(String fileName) {
        boolean isObjectExist = amazonS3Client.doesObjectExist(bucket, fileName);

        if (isObjectExist) {
            amazonS3Client.deleteObject(bucket, fileName);
        }
    }

}