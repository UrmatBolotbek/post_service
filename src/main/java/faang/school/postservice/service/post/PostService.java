package faang.school.postservice.service.post;

import faang.school.postservice.dto.post.PostFilterDto;
import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.dto.post.PostUpdateDto;
import faang.school.postservice.dto.resource.ResourceResponseDto;
import faang.school.postservice.mapper.post.PostMapper;
import faang.school.postservice.mapper.resource.ResourceMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.post.filter.PostFilters;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.service.resource.ResourceService;
import faang.school.postservice.service.s3.S3Service;
import faang.school.postservice.validator.post.PostValidator;
import faang.school.postservice.validator.resource.ResourceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final ResourceService resourceService;
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final ResourceMapper resourceMapper;
    private final ResourceValidator resourceValidator;
    private final PostValidator postValidator;
    private final List<PostFilters> postFilters;
    private final ModerationDictionary moderationDictionary;

    public PostResponseDto create(PostRequestDto requestDto, List<MultipartFile> images, List<MultipartFile> audio) {
        postValidator.validateCreate(requestDto);
        Post post = postMapper.toEntity(requestDto);

        post.setPublished(false);
        post.setDeleted(false);
        post.setLikes(new ArrayList<>());
        post.setComments(new ArrayList<>());
        post.setResources(new ArrayList<>());

        post = postRepository.save(post);

        uploadResourcesToPost(images, "image", post);
        uploadResourcesToPost(audio, "audio", post);

        log.info("Post with id {} created", post.getId());
        post = postRepository.save(post);

        PostResponseDto responseDto = postMapper.toDto(post);
        populateResourceUrls(responseDto, post);
        return responseDto;
    }

    public PostResponseDto updatePost(Long postId, PostUpdateDto updateDto, List<MultipartFile> images, List<MultipartFile> audio) {
        Post post = postRepository.getPostById(postId);

        if (updateDto.getContent() != null) {
            post.setContent(updateDto.getContent());
        }

        deleteResourcesFromPost(updateDto.getImageFilesIdsToDelete());
        deleteResourcesFromPost(updateDto.getAudioFilesIdsToDelete());

        uploadResourcesToPost(images, "image", post);
        uploadResourcesToPost(audio, "audio", post);

        resourceValidator.validateResourceCounts(post);

        post.setUpdatedAt(LocalDateTime.now());
        post = postRepository.save(post);
        log.info("Post with id {} updated", postId);

        PostResponseDto responseDto = postMapper.toDto(post);
        populateResourceUrls(responseDto, post);
        return responseDto;
    }

    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.getPostById(postId);
        PostResponseDto responseDto = postMapper.toDto(post);
        populateResourceUrls(responseDto, post);
        return responseDto;
    }

    private void uploadResourcesToPost(List<MultipartFile> files, String resourceType, Post post) {
        if (files != null) {
            log.info("Uploading {} {} resources for post ID {}", files.size(), resourceType, post.getId());
            List<Resource> resources = resourceService.uploadResources(files, resourceType, post);
            post.getResources().addAll(resources);
            log.info("{} {} resources uploaded successfully for post ID {}", resources.size(), resourceType, post.getId());
        }
    }

    private void populateResourceUrls(PostResponseDto responseDto, Post post) {
        List<ResourceResponseDto> imageResources = post.getResources().stream()
                .filter(resource -> "image".equals(resource.getType()))
                .map(this::mapResourceToDto)
                .toList();

        List<ResourceResponseDto> audioResources = post.getResources().stream()
                .filter(resource -> "audio".equals(resource.getType()))
                .map(this::mapResourceToDto)
                .toList();

        responseDto.setImages(imageResources);
        responseDto.setAudio(audioResources);
    }

    private ResourceResponseDto mapResourceToDto(Resource resource) {
        ResourceResponseDto dto = resourceMapper.toDto(resource);
        dto.setDownloadUrl(s3Service.generatePresignedUrl(resource.getKey()));
        return dto;
    }

    private void deleteResourcesFromPost(List<Long> resourceIds) {
        if (resourceIds != null) {
            log.info("Deleting {} resources", resourceIds.size());
            resourceService.deleteResources(resourceIds);
            log.info("{} resources deleted successfully", resourceIds.size());
        }
    }

    public PostResponseDto publishPost(Long id) {
        Post post = postValidator.validateAndGetPostById(id);
        postValidator.validatePublish(post);
        post.setPublished(true);
        post.setDeleted(false);

        return postMapper.toDto(postRepository.save(post));
    }

    public void deletePost(Long id) {
        Post post = postRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
        postValidator.validateDelete(post);

        post.setPublished(false);
        post.setDeleted(true);
        postRepository.save(post);
    }

    public PostResponseDto getPostById(Long id) {
        return postRepository.findById(id)
                .map(postMapper::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    public List<PostResponseDto> getPosts(PostFilterDto filterDto) {
        Stream<Post> posts = StreamSupport.stream(postRepository.findAll().spliterator(), false);

        postFilters.stream()
                .filter(filter -> filter.isApplicable(filterDto))
                .forEach(filter -> filter.apply(posts, filterDto));

        return postMapper.toListPostDto(posts.toList());
    }

    @Async("moderationPool")
    public void verifyPostsForModeration(List<Post> posts) {
        posts.forEach(post -> {
            post.setVerifiedDate(LocalDateTime.now());
            boolean isVerified = moderationDictionary.isVerified(post.getContent());
            post.setVerified(isVerified);
            log.info("Post with id {} has been verified and has status {}", post.getId(), isVerified);
            postRepository.save(post);
        });
    }
}