package faang.school.postservice.validator.post;

import faang.school.postservice.client.ProjectServiceClient;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.excaption.post.PostException;
import faang.school.postservice.model.Hashtag;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import jakarta.persistence.EntityExistsException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Data
@Component
@Slf4j
public class PostValidator {
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final PostRepository postRepository;


    public Post validateAndGetPostById(Long id) {
        return postRepository.getPostById(id);
    }

    public void validateUserExist(Long id) {
        userServiceClient.getUser(id);
    }

    public void validateProjectExist(Long id) {
        projectServiceClient.getProject(id);
    }

    public void validateCreate(PostRequestDto postRequestDto) {
        if (postRequestDto.getAuthorId() != null) {
            userServiceClient.getUser(postRequestDto.getAuthorId());
        } else {
            projectServiceClient.getProject(postRequestDto.getProjectId());
        }

    }

    public void validateUserToPost(Post post, long authorId) {
        if (post.getAuthorId() != authorId) {
            throw new EntityExistsException("Post not found with authorId: " + authorId);
        }
    }


    public void validatePublish(Post post) {
        if (post.isPublished()) {
            throw new PostException("Post is already published");
        }
    }

    public void validateDelete(Post post) {
        if (post.isDeleted()) {
            throw new PostException("Post already deleted");
        }
    }

    public void validatePostHatThisHashtag(Post post, Hashtag hashtag) {
        if (post.getHashtags().contains(hashtag)) {
            log.warn("Hashtag with title {} by postId {} is already in use", hashtag.getTitle(), post.getId());
            throw new PostException("Hashtag with title "
                    + hashtag.getTitle() + " by postId " + post.getId() + " already exists");
        }
    }

}
