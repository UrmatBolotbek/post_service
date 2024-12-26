package faang.school.postservice.controller.hashtag;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.service.hashtag.HashtagService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;
    private final UserContext context;

    @GetMapping("/posts")
    public List<PostResponseDto> getPostsByHashtag(@RequestParam String hashtag) {
        return hashtagService.getPostsByHashtag(hashtag);
    }

    @PostMapping("/post/{postId}")
    public void createHashtagToPost(@RequestParam String hashtag, @PathVariable Long postId) {
        hashtagService.createHashtagToPost(hashtag, postId, context.getUserId());
    }

    @GetMapping("/post/{postId}")
    public List<String> getAllHashtagByPostId(@PathVariable Long postId) {
       return hashtagService.getAllHashtagByPostId(postId, context.getUserId());
    }

}
