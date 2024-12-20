package faang.school.postservice.validator.resource;

import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceValidator {

    private static final int MAX_IMAGES = 10;
    private static final int MAX_AUDIO_FILES = 5;

    public void validateImageCount(Post post) {
        long imageCount = post.getResources().stream()
                .filter(resource -> "image".equalsIgnoreCase(resource.getType()))
                .count();

        if (imageCount > MAX_IMAGES) {
            throw new DataValidationException("A post cannot have more than " + MAX_IMAGES + " images.");
        }
    }

    public void validateAudioCount(Post post) {
        long audioCount = post.getResources().stream()
                .filter(resource -> "audio".equalsIgnoreCase(resource.getType()))
                .count();

        if (audioCount > MAX_AUDIO_FILES) {
            throw new DataValidationException("A post cannot have more than " + MAX_AUDIO_FILES + " audio files.");
        }
    }

    public void validateResourceCounts(Post post) {
        validateImageCount(post);
        validateAudioCount(post);
    }
}