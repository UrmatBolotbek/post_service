package faang.school.postservice.service.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import faang.school.postservice.exception.FileDeletionException;
import faang.school.postservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;
    @Value("${services.s3.bucketName}")
    private String bucketName;

    public void uploadFile(MultipartFile file, String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            s3Client.putObject(putObjectRequest);
            log.info("File {} uploaded successfully with key {}", file.getOriginalFilename(), key);
        } catch (Exception e) {
            log.error("Error occurred while uploading file {} with key {}", file.getOriginalFilename(), key, e);
            throw new ResourceNotFoundException("Error uploading file to S3");
        }
    }

    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
            log.info("Successfully deleted file with key {} from S3", key);
        } catch (Exception e) {
            log.error("Error occurred while deleting file with key {} from S3", key, e);
            throw new FileDeletionException("Error deleting file from S3");
        }
    }

    public String generatePresignedUrl(String key) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);

            GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(presignedUrlRequest);
            log.info("Generated pre-signed URL for key {}: {}", key, url.toString());
            return url.toString();
        } catch (Exception e) {
            log.error("Error occurred while generating pre-signed URL for key {}", key, e);
            throw new RuntimeException("Error generating pre-signed URL", e);
        }
    }
}