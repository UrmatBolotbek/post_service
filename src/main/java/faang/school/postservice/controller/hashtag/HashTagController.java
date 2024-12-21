package faang.school.postservice.controller.hashtag;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.service.hashtag.HashtagService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hashtags")
public class HashTagController {

    private final HashtagService hashtagService;
    private final UserContext context;

    @GetMapping("/{hashtag}")
    public List<PostResponseDto> getPostsByHashtag(@PathVariable String hashtag) {
        return hashtagService.getPostsByHashtag(hashtag);
    }

    @PostMapping("/{hashtag}/post/{postId}")
    public void createHashtagToPost(@PathVariable String hashtag, @PathVariable Long postId) {
        hashtagService.createHashtagToPost(hashtag, postId, context.getUserId());
    }

    @GetMapping("/post/{postId}")
    public List<String> getAllHashtagByPostId(@PathVariable Long postId) {
       return hashtagService.getAllHashtagByPostId(postId, context.getUserId());
    }

}
