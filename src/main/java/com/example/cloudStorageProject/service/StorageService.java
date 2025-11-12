package com.example.cloudStorageProject.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

    private final Storage storage;
    private final String bucketName;

    public StorageService(
            @Value("${gcp.storage.bucket}") String bucketName,
            @Value("${gcp.storage.credentials}") Resource credentialsResource) throws IOException {

        this.bucketName = bucketName;

        // Load credentials from resources/service-account.json
        this.storage = StorageOptions.newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(credentialsResource.getInputStream()))
                .build()
                .getService();
    }

    public String uploadFile(MultipartFile file, String targetFilename) throws IOException {
        BlobId blobId = BlobId.of(bucketName, targetFilename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        return String.format("gs://%s/%s", bucketName, targetFilename);
    }

    public List<String> listFiles(String prefix) {
        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();
        List<String> fileNames = new ArrayList<>();
        for (Blob blob : blobs) {
            fileNames.add(blob.getName());
        }
        return fileNames;
    }

    public byte[] downloadFile(String objectName) {
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        return blob == null ? null : blob.getContent();
    }

    public boolean deleteFile(String objectName) {
        return storage.delete(BlobId.of(bucketName, objectName));
    }
}
