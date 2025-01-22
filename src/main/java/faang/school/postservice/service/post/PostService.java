package faang.school.postservice.service.post;

import faang.school.postservice.dto.post.PostFilterDto;
import faang.school.postservice.config.api.SpellingConfig;
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
import faang.school.postservice.service.resource.ResourceService;
import faang.school.postservice.service.s3.S3Service;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.validator.post.PostValidator;
import faang.school.postservice.validator.resource.ResourceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final ExecutorService spellingExecutor;
    private final ResourceService resourceService;
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final SpellingConfig api;
    private final RestTemplate restTemplate;
    private final ResourceMapper resourceMapper;
    private final ResourceValidator resourceValidator;
    private final PostValidator postValidator;
    private final List<PostFilters> postFilters;
    private final ModerationDictionary moderationDictionary;

    @Transactional
    public PostResponseDto create(PostRequestDto requestDto, List<MultipartFile> images, List<MultipartFile> audio) {
        postValidator.validateCreate(requestDto);
        Post post = postMapper.toEntity(requestDto);
        post.setPublished(false);
        post.setDeleted(false);
        post.setLikes(new ArrayList<>());
        post.setComments(new ArrayList<>());
        post.setResources(new ArrayList<>());
        post.setHashtags(new ArrayList<>());
        post = postRepository.save(post);
        uploadResourcesToPost(images, "image", post);
        uploadResourcesToPost(audio, "audio", post);
        post = postRepository.save(post);
        log.info("Post with id {} created", post.getId());
        PostResponseDto responseDto = postMapper.toDto(post);
        populateResourceUrls(responseDto, post);
        return responseDto;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.getPostById(postId);
        PostResponseDto responseDto = postMapper.toDto(post);
        populateResourceUrls(responseDto, post);
        return responseDto;
    }

    @Transactional
    public PostResponseDto publishPost(Long id) {
        Post post = postValidator.validateAndGetPostById(id);
        postValidator.validatePublish(post);
        post.setPublished(true);
        post.setDeleted(false);
        post = postRepository.save(post);
        return postMapper.toDto(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        postValidator.validateDelete(post);
        post.setPublished(false);
        post.setDeleted(true);
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        return postRepository.findById(id)
                .map(postMapper::toDto)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void checkSpelling() {
        List<Post> posts = postRepository.findByPublishedFalse();
        int sizeOfRequests = getSizeOfRequest(posts.size());
        for (int i = 0; i < posts.size(); i += sizeOfRequests) {
            List<Post> sublist = posts.subList(i, Math.min(i + sizeOfRequests, posts.size()));
            checkingPostsForSpelling(sublist);
        }
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getPosts(PostFilterDto filterDto) {
        Stream<Post> postsStream = StreamSupport.stream(postRepository.findAll().spliterator(), false);
        postFilters.stream()
                .filter(filter -> filter.isApplicable(filterDto))
                .forEach(filter -> filter.apply(postsStream, filterDto));
        return postMapper.toListPostDto(postsStream.toList());
    }

    @Async("moderationPool")
    @Transactional
    public void verifyPostsForModeration(List<Post> posts) {
        posts.forEach(post -> {
            post.setVerifiedDate(LocalDateTime.now());
            boolean isVerified = moderationDictionary.isVerified(post.getContent());
            post.setVerified(isVerified);
            log.info("Post with id {} has been verified and has status {}", post.getId(), isVerified);
            postRepository.save(post);
        });
    }

    private void uploadResourcesToPost(List<MultipartFile> files, String resourceType, Post post) {
        if (files != null && !files.isEmpty()) {
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
        if (resourceIds != null && !resourceIds.isEmpty()) {
            log.info("Deleting {} resources", resourceIds.size());
            resourceService.deleteResources(resourceIds);
            log.info("{} resources deleted successfully", resourceIds.size());
        }
    }

    private void checkingPostsForSpelling(List<Post> posts) {
        String jsonPayload = getJsonFromPosts(posts);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Content-Type", api.getContent());
        headers.set("x-rapidapi-host", api.getHost());
        headers.set("x-rapidapi-key", api.getKey());
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
        try {
            String response = restTemplate.postForObject(api.getEndpoint(), requestEntity, String.class);
            JSONObject jsonObject = new JSONObject(response);
            int errorCount = jsonObject.getInt("spellingErrorCount");
            if (errorCount == 0) {
                log.info("No errors found in post content");
                return;
            }
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                int finalI = i;
                spellingExecutor.execute(() -> setCorrectContent(jsonObject, post, finalI));
            }
            spellingExecutor.shutdown();
            if (!spellingExecutor.awaitTermination(2, TimeUnit.MINUTES)) {
                spellingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("An interrupt error occurred in the method of checking the spelling ", e);
            spellingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (HttpClientErrorException e) {
            log.error("An error occurred while executing a request to an external server ", e);
            throw new HttpClientErrorException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (JSONException e) {
            log.error("An error occurred while processing JSON content. ", e);
            throw new JSONException(e);
        }
    }

    private String getJsonFromPosts(List<Post> posts) {
        List<String> contentFromPosts = new ArrayList<>();
        posts.forEach(post -> contentFromPosts.add(post.getContent()));
        JSONObject json = new JSONObject();
        json.put("language", "enUS");
        JSONArray fieldvalues = new JSONArray();
        contentFromPosts.forEach(content -> fieldvalues.put(escapeJson(content)));
        json.put("fieldvalues", fieldvalues);
        JSONObject config = new JSONObject();
        config.put("forceUpperCase", false)
                .put("ignoreIrregularCaps", false)
                .put("ignoreFirstCaps", true)
                .put("ignoreNumbers", true)
                .put("ignoreUpper", false)
                .put("ignoreDouble", false)
                .put("ignoreWordsWithNumbers", true);
        json.put("config", config);
        return json.toString();
    }

    private void setCorrectContent(JSONObject jsonObject, Post post, int index) {
        try {
            String content = post.getContent();
            JSONArray elementsArray = jsonObject.getJSONArray("elements");
            JSONObject firstElement = elementsArray.getJSONObject(index);
            JSONArray errorsArray = firstElement.getJSONArray("errors");
            if (errorsArray.isEmpty()) {
                return;
            }
            for (int i = 0; i < errorsArray.length(); i++) {
                JSONObject error = errorsArray.getJSONObject(i);
                String word = error.getString("word");
                JSONArray suggestionsArray = error.getJSONArray("suggestions");
                String correctWord = suggestionsArray.getString(0);
                content = content.replace(word, correctWord);
            }
            post.setContent(content);
            postRepository.save(post);
            log.info("Added corrected content {} to the post {}", post.getContent(), post.getId());
        } catch (Exception e) {
            log.error("An error occurred while processing the post {}", post.getId(), e);
        }
    }

    private int getSizeOfRequest(int sizeOfPosts) {
        if (sizeOfPosts <= 100) {
            return 10;
        } else if (sizeOfPosts <= 500) {
            return 50;
        } else {
            return 100;
        }
    }

    private static String escapeJson(String data) {
        return data.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
