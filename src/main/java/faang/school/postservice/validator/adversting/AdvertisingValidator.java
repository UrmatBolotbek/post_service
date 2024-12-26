package faang.school.postservice.validator.adversting;

import faang.school.postservice.dto.adversting.AdvertisingRequestDto;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.repository.ad.AdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class AdvertisingValidator {
    private final AdRepository adRepository;
    private final PostRepository postRepository;

    public void validatePostForAdvertising(long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post with id " + postId + " does not exist");
        }

        Optional<Ad> existingAd = adRepository.findByPostId(postId);
        if (existingAd.isPresent()) {
            throw new IllegalStateException("This post is already being advertised");
        }
    }

    public void validateDate(AdvertisingRequestDto request){
        if (request.getDays() <= 0) {
            throw new IllegalArgumentException("Days must be a positive number.");
        }

    }
}
