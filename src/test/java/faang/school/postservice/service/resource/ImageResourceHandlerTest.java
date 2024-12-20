package faang.school.postservice.service.resource;


import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.service.s3.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageResourceHandlerTest {

    @InjectMocks
    private ImageResourceHandler imageResourceHandler;

    @Mock
    private S3Service s3Service;

    @Test
    void shouldAddImageResourceAndConstructFolderAndKeyCorrectly() throws IOException {
        Long postId = 1L;
        String filename = "image.jpg";
        Post post = new Post();
        post.setId(postId);

        BufferedImage validImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(validImage, "jpeg", baos);

        MockMultipartFile file = new MockMultipartFile("file", filename, "image/jpeg", new ByteArrayInputStream(baos.toByteArray()));

        String expectedFolder = "Post: 1/image";
        String expectedKeyPrefix = String.format("%s/", expectedFolder);

        Resource resource = imageResourceHandler.addResource(file, post);

        assertNotNull(resource);
        assertTrue(resource.getKey().startsWith(expectedKeyPrefix));
        assertTrue(resource.getKey().contains("_" + filename));
        verify(s3Service, times(1)).uploadFile(any(MultipartFile.class), contains(expectedKeyPrefix));
    }

    @Test
    void shouldResizeLargeImageAndAddResourceSuccessfully() throws IOException {
        Long postId = 1L;
        String filename = "largeImage.jpg";
        Post post = new Post();
        post.setId(postId);

        BufferedImage largeImage = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(largeImage, "jpeg", baos);

        MockMultipartFile file = new MockMultipartFile("file", filename, "image/jpeg", new ByteArrayInputStream(baos.toByteArray()));

        Resource resource = imageResourceHandler.addResource(file, post);

        assertNotNull(resource);
        assertEquals("image", resource.getType());
        verify(s3Service, times(1)).uploadFile(any(MultipartFile.class), anyString());
    }

    @Test
    void shouldNotResizeSmallImageAndAddResourceSuccessfully() throws IOException {
        Long postId = 1L;
        String filename = "smallImage.jpg";
        Post post = new Post();
        post.setId(postId);

        BufferedImage smallImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(smallImage, "jpeg", baos);

        MockMultipartFile file = new MockMultipartFile("file", filename, "image/jpeg", new ByteArrayInputStream(baos.toByteArray()));

        Resource resource = imageResourceHandler.addResource(file, post);

        assertNotNull(resource);
        assertEquals("image", resource.getType());
        verify(s3Service, times(1)).uploadFile(any(MultipartFile.class), anyString());
    }
}