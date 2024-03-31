package com.team.leaf.aws.s3.controller;

import com.team.leaf.aws.s3.service.S3Service;
import jakarta.servlet.annotation.MultipartConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service service;

    @PostMapping("/s3")
    public void aaaa(@RequestPart MultipartFile file) throws IOException {
        service.uploadImageToS3(file);
    }

    @DeleteMapping("/s3/{fileName}")
    public void bbbb(@PathVariable String fileName) throws IOException {
        service.deleteFile(fileName);
    }

}
