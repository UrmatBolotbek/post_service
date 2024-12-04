package faang.school.postservice.controller.like;

import faang.school.postservice.dto.like.LikeRequestDto;
import faang.school.postservice.dto.like.LikeResponseDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/post/{postId}")
    public LikeResponseDto postLike(@RequestBody LikeRequestDto likeDto, @PathVariable long postId) {
      return likeService.postLike(likeDto, postId);
    }

    @DeleteMapping("/post/{postId}")
    public void deleteLikeFromPost(@RequestBody LikeRequestDto likeDto, @PathVariable long postId) {
        likeService.deleteLikeFromPost(likeDto, postId);
    }

    @GetMapping("/post/{postId}/likes")
    public List<UserDto> getUsersByPostLikes(@PathVariable long postId) {
        return likeService.getUsersByPostId(postId);
    }

    @PostMapping("/comment/{commentId}")
    public LikeResponseDto commentLike(@RequestBody LikeRequestDto likeDto, @PathVariable long commentId) {
        return likeService.commentLike(likeDto, commentId);
    }

    @DeleteMapping("/comment/{commentId}")
    public void deleteLikeFromComment(@RequestBody LikeRequestDto likeDto, @PathVariable long commentId) {
         likeService.deleteLikeFromComment(likeDto, commentId);
    }

    @GetMapping("/comment/{commentId}/likes")
    public List<UserDto> getUsersByCommentLikes(@PathVariable long commentId) {
        return likeService.getUsersByCommentId(commentId);
    }
}
