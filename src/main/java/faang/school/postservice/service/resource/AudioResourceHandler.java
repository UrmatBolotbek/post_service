package faang.school.postservice.service.resource;

import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioResourceHandler implements ResourceHandler {
    private final S3Service s3Service;
    private final static String TYPE = "audio";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Resource addResource(MultipartFile file, Post post) {
        String folder = constructFolder(post.getId());
        String key = constructKey(folder, file.getOriginalFilename());

        s3Service.uploadFile(file, key);

        Resource resource = new Resource();
        resource.setKey(key);
        resource.setSize(file.getSize());
        resource.setCreatedAt(LocalDateTime.now());
        resource.setName(file.getOriginalFilename());
        resource.setType("audio");

        log.info("Audio resource created with key {}", key);
        return resource;
    }

    private String constructFolder(Long postId) {
        return String.format("Post: %d/audio", postId);
    }

    private String constructKey(String folder, String filename) {
        return String.format("%s/%d_%s", folder, System.currentTimeMillis(), filename);
    }
}
