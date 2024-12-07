package faang.school.postservice.service.resource;

import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import org.springframework.web.multipart.MultipartFile;


public interface ResourceHandler {
    Resource addResource(MultipartFile file, Post post);

    String getType();
}