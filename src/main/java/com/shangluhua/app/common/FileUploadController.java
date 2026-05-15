package com.shangluhua.app.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shangluhua.app.common.ApiException;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException("请选择文件");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(fileName);
            file.transferTo(targetPath.toFile());
            String url = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            throw new ApiException("上传失败: " + e.getMessage());
        }
    }
}
