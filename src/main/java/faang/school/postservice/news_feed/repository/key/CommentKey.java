package faang.school.postservice.news_feed.repository.key;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CommentKey extends CacheKey {
    @Value("${app.post.cache.news_feed.prefix.comment_id}")
    private String commentIdPrefix;

    @Value("${app.post.cache.news_feed.prefix.post_comments_ids_set}")
    private String commentsIdsSetPrefix;

    @Value("${app.post.cache.news_feed.postfix.likes}")
    private String likesPostfix;

    @Value("${app.post.cache.news_feed.prefix.post_id}")
    private String postIdPrefix;

    public String build(long commentId) {
        return buildKey(commentIdPrefix, commentId, null);
    }

    public String buildLikesCounterKey(long commentId) {
        return buildKey(commentIdPrefix, commentId, likesPostfix);
    }

    public String buildPostCommentsSetKey(long postId) {
        return buildKey(commentsIdsSetPrefix + postIdPrefix, postId, null);
    }

    public String getCommentKeyFrom(String counterKey) {
        return counterKey.split("/")[0];
    }

    public String getCommentLikeCounterPattern() {
        return commentIdPrefix + "*" + likesPostfix;
    }
}
