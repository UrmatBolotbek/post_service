package faang.school.postservice.service.resource;

import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.repository.ResourceRepository;
import faang.school.postservice.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceUploadHandlerFactory handlerFactory;
    private final S3Service s3Service;

    public List<Resource> uploadResources(List<MultipartFile> files, String resourceType, Post post) {
        ResourceHandler handler = handlerFactory.getHandler(resourceType);
        return files.stream()
                .map(file -> {
                    Resource resource = handler.addResource(file, post);
                    resource.setPost(post);
                    resourceRepository.save(resource);
                    return resource;
                })
                .toList();
    }

    public void deleteResources(List<Long> resourceIds) {
        resourceIds.forEach(resourceId -> {
            Resource resource = resourceRepository.getReferenceById(resourceId);
            s3Service.deleteFile(resource.getKey());
            log.info("Deleted resource with key {} from S3", resource.getKey());
            resourceRepository.delete(resource);
            log.info("Deleted resource with ID {} from repository", resourceId);
        });
    }
}