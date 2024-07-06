package pl.jakubdudek.blogappbackend.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadFile(MultipartFile file) throws IOException
    {
        if(file.isEmpty()) {
            return "File is empty";
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID()+"_"+originalFileName;

        Path destPath = Paths.get(uploadDir + uniqueFileName).toAbsolutePath().normalize();
        Files.createDirectories(destPath.getParent());
        file.transferTo(destPath);

        return uniqueFileName;
    }

    public Resource downloadFile(String uniqueFileName) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir + uniqueFileName).toAbsolutePath().normalize();
        Resource file = new UrlResource(filePath.toUri());

        if(file.exists() && file.isReadable()) {
            return file;
        }
        else {
            throw new EntityNotFoundException("File not found");
        }
    }

    public void deleteFile(String uniqueFileName) throws IOException {
        Path filePath = Paths.get(uploadDir + uniqueFileName).toAbsolutePath().normalize();
        Files.deleteIfExists(filePath);
    }
}
