package com.example.cloudStorageProject.controller;



import com.example.cloudStorageProject.service.StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    // Upload file
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "name", required = false) String name) {
        try {
            String filename = (name == null || name.isBlank()) ? file.getOriginalFilename() : name;
            String uri = storageService.uploadFile(file, filename);
            return ResponseEntity.ok("✅ Uploaded successfully to: " + uri);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("❌ Upload failed: " + e.getMessage());
        }
    }

    // List all files
    @GetMapping("/list")
    public ResponseEntity<List<String>> list(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix) {
        return ResponseEntity.ok(storageService.listFiles(prefix));
    }

    // Download file
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("name") String name) {
        byte[] content = storageService.downloadFile(name);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    // Delete file
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam("name") String name) {
        boolean deleted = storageService.deleteFile(name);
        return deleted ?
                ResponseEntity.ok("🗑️ File deleted: " + name) :
                ResponseEntity.status(404).body("❌ File not found: " + name);
    }
}

