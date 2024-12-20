package faang.school.postservice.service.resource;

import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.repository.ResourceRepository;
import faang.school.postservice.service.s3.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceUploadHandlerFactory handlerFactory;

    @Mock
    private S3Service s3Service;

    @Mock
    private ResourceHandler resourceHandler;

    @Test
    void shouldUploadResourcesSuccessfully() {
        String resourceType = "image";
        Post post = new Post();
        post.setId(1L);

        MockMultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "image data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "image data".getBytes());

        Resource resource1 = new Resource();
        resource1.setKey("key1");
        resource1.setType(resourceType);
        resource1.setName("image1.jpg");

        Resource resource2 = new Resource();
        resource2.setKey("key2");
        resource2.setType(resourceType);
        resource2.setName("image2.jpg");

        when(handlerFactory.getHandler(resourceType)).thenReturn(resourceHandler);
        when(resourceHandler.addResource(file1, post)).thenReturn(resource1);
        when(resourceHandler.addResource(file2, post)).thenReturn(resource2);

        List<Resource> uploadedResources = resourceService.uploadResources(List.of(file1, file2), resourceType, post);

        assertEquals(2, uploadedResources.size());
        verify(resourceHandler).addResource(file1, post);
        verify(resourceHandler).addResource(file2, post);
        verify(resourceRepository).save(resource1);
        verify(resourceRepository).save(resource2);
    }

    @Test
    void shouldDeleteResourcesSuccessfully() {
        Long resourceId1 = 1L;
        Long resourceId2 = 2L;

        Resource resource1 = new Resource();
        resource1.setId(resourceId1);
        resource1.setKey("key1");

        Resource resource2 = new Resource();
        resource2.setId(resourceId2);
        resource2.setKey("key2");

        when(resourceRepository.getReferenceById(resourceId1)).thenReturn(resource1);
        when(resourceRepository.getReferenceById(resourceId2)).thenReturn(resource2);

        resourceService.deleteResources(List.of(resourceId1, resourceId2));

        verify(s3Service).deleteFile("key1");
        verify(s3Service).deleteFile("key2");
        verify(resourceRepository).delete(resource1);
        verify(resourceRepository).delete(resource2);
    }

    @Test
    void shouldThrowExceptionIfHandlerNotFoundDuringUpload() {
        String resourceType = "invalid-type";
        Post post = new Post();
        post.setId(1L);

        MockMultipartFile file = new MockMultipartFile("file", "invalid.jpg", "image/jpeg", "image data".getBytes());

        when(handlerFactory.getHandler(resourceType)).thenThrow(new IllegalArgumentException("Handler not found"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> resourceService.uploadResources(List.of(file), resourceType, post));
        assertEquals("Handler not found", exception.getMessage());
        verifyNoInteractions(resourceRepository, s3Service);
    }

    @Test
    void shouldHandleEmptyResourceListDuringDeletion() {
        resourceService.deleteResources(Collections.emptyList());

        verifyNoInteractions(resourceRepository, s3Service);
    }
}