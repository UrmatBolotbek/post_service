package faang.school.postservice.service.comment;

import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.validator.comment.CommentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private static final Long VALID_COMMENT_ID = 1L;
    private static final Long VALID_POST_ID = 22L;
    private static final String VALID_CONTENT = "some content";
    private static final String UPDATED_CONTENT = "some other content";
    private static final LocalDateTime CREATED_AT_FOR_OLDER_COMMENT =
            LocalDateTime.of(2023, 11, 10, 10, 0);
    private static final LocalDateTime CREATED_AT_FOR_NEWER_COMMENT =
            LocalDateTime.of(2024, 11, 11, 10, 0);
    private static final UserDto VALID_USER_DTO =
            new UserDto(1L, "John Doe", "JohnDoe@gmail.com", "1234567", 5);

    @Test
    void createComment_shouldCreateCommentSuccessfully() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setPostId(VALID_COMMENT_ID);
        commentRequestDto.setAuthorId(VALID_USER_DTO.getId());
        commentRequestDto.setContent("Test Content");

        Comment comment = new Comment();
        comment.setId(VALID_COMMENT_ID);
        comment.setLikes(new ArrayList<>());

        CommentResponseDto expectedOutput = new CommentResponseDto();
        expectedOutput.setId(VALID_COMMENT_ID);

        when(commentMapper.toEntity(commentRequestDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(expectedOutput);

        CommentResponseDto actualOutput = commentService.createComment(commentRequestDto);

        verify(commentValidator).validateAuthorExists(commentRequestDto.getAuthorId());
        verify(commentValidator).validatePostExists(commentRequestDto.getPostId());
        verify(commentRepository).save(comment);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void updateComment_shouldUpdateCommentSuccessfully() {
        CommentUpdateRequestDto commentUpdateRequestDto = new CommentUpdateRequestDto();
        commentUpdateRequestDto.setCommentId(VALID_COMMENT_ID);
        commentUpdateRequestDto.setContent(UPDATED_CONTENT);

        Comment existingComment = new Comment();
        existingComment.setId(VALID_COMMENT_ID);
        existingComment.setContent(VALID_CONTENT);

        CommentResponseDto expectedOutput = new CommentResponseDto();

        when(commentRepository.getCommentById(commentUpdateRequestDto.getCommentId())).thenReturn(existingComment);
        when(commentMapper.toDto(existingComment)).thenReturn(expectedOutput);

        CommentResponseDto actualOutput = commentService.updateComment(commentUpdateRequestDto);

        verify(commentRepository).save(existingComment);
        assertEquals(UPDATED_CONTENT, existingComment.getContent());
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void getCommentsByPostId_shouldReturnCommentsInDescendingOrder() {
        Long postId = VALID_POST_ID;

        Comment comment1 = new Comment();
        comment1.setCreatedAt(CREATED_AT_FOR_OLDER_COMMENT);

        Comment comment2 = new Comment();
        comment2.setCreatedAt(CREATED_AT_FOR_NEWER_COMMENT);

        List<Comment> comments = new ArrayList<>();
        comments.add(comment1);
        comments.add(comment2);

        CommentResponseDto dto1 = new CommentResponseDto();
        CommentResponseDto dto2 = new CommentResponseDto();

        when(commentRepository.findAllByPostId(postId)).thenReturn(comments);
        when(commentMapper.toDto(comments)).thenReturn(List.of(dto2, dto1)); // In reversed order

        List<CommentResponseDto> actualOutput = commentService.getCommentsByPostId(postId);

        verify(commentValidator).validatePostExists(postId);
        comments.sort(Comparator.comparing(Comment::getCreatedAt).reversed());
        assertEquals(List.of(dto2, dto1), actualOutput);
    }

    @Test
    void deleteComment_shouldDeleteCommentSuccessfully() {
        Long commentId = VALID_COMMENT_ID;

        commentService.deleteComment(commentId);

        verify(commentRepository).deleteById(commentId);
        verifyNoMoreInteractions(commentRepository);
    }
}