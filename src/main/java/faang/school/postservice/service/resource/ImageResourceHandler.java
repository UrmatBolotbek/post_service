package faang.school.postservice.service.resource;

import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResourceHandler implements ResourceHandler {
    private final S3Service s3Service;
    private final static String TYPE = "image";

    private static final int MAX_HORIZONTAL_WIDTH = 1080;
    private static final int MAX_HORIZONTAL_HEIGHT = 566;
    private static final int MAX_SQUARE_SIZE = 1080;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Resource addResource(MultipartFile file, Post post) {
        String folder = constructFolder(post.getId());
        String key = constructKey(folder, file.getOriginalFilename());

        MultipartFile processedFile = processImage(file);

        s3Service.uploadFile(processedFile, key);

        Resource resource = new Resource();
        resource.setKey(key);
        resource.setSize(file.getSize());
        resource.setCreatedAt(LocalDateTime.now());
        resource.setName(file.getOriginalFilename());
        resource.setType("image");

        log.info("Image resource created with key {}", key);
        return resource;
    }

    private String constructFolder(Long postId) {
        return String.format("Post: %d/image", postId);
    }

    private String constructKey(String folder, String filename) {
        return String.format("%s/%d_%s", folder, System.currentTimeMillis(), filename);
    }

    private MultipartFile processImage(MultipartFile file) {
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Image is null");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (isResizingNeeded(width, height)) {
                int[] newDimensions = calculateNewDimensions(width, height);
                int newWidth = newDimensions[0];
                int newHeight = newDimensions[1];

                log.info("Resizing image {} to dimensions {}X{}", file.getOriginalFilename(), newWidth, newHeight);

                BufferedImage resizedImage = Thumbnails.of(originalImage)
                        .size(newWidth, newHeight)
                        .asBufferedImage();
                return convertBufferedImageToMultipartFile(resizedImage, file);
            }
            return file;

        } catch (IOException e) {
            log.error("Error processing image {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new IllegalArgumentException("Error processing image " + file.getOriginalFilename(), e);
        }
    }

    private boolean isResizingNeeded(int width, int height) {
        boolean isSquare = width == height;
        if (isSquare) {
            return width > MAX_SQUARE_SIZE;
        } else {
            return width > MAX_HORIZONTAL_HEIGHT || height > MAX_HORIZONTAL_HEIGHT;
        }
    }

    private int[] calculateNewDimensions(int width, int height) {
        boolean isSquare = width == height;
        if (isSquare) {
            return new int[]{width, height};
        } else {
            float aspectRatio = (float) width / (float) height;
            if (aspectRatio > 1) {
                return new int[]{MAX_HORIZONTAL_WIDTH, (int) (MAX_HORIZONTAL_WIDTH / aspectRatio)};
            } else {
                return new int[]{(int) (MAX_HORIZONTAL_HEIGHT * aspectRatio), MAX_HORIZONTAL_HEIGHT};
            }
        }
    }

    private MultipartFile convertBufferedImageToMultipartFile(BufferedImage image, MultipartFile originalFile) throws IOException {
        String fileExtension = getFileExtension(Objects.requireNonNull(originalFile.getOriginalFilename()));
        String imageFormat = "jpeg";
        if ("png".equalsIgnoreCase(fileExtension)) {
            imageFormat = "png";
        } else if ("jpg".equalsIgnoreCase(fileExtension) || "jpeg".equalsIgnoreCase(fileExtension)) {
            imageFormat = "jpeg";
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, imageFormat, outputStream);
        return new MultipartFileAdapter(outputStream.toByteArray(), originalFile.getOriginalFilename(), "image/" + imageFormat);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}