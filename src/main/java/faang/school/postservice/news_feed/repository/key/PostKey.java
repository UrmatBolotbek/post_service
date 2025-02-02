package faang.school.postservice.news_feed.repository.key;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PostKey extends CacheKey {
    @Value("${app.post.cache.news_feed.prefix.post_id}")
    private String postIdPrefix;

    @Value("${app.post.cache.news_feed.postfix.views}")
    private String viewsPostfix;

    @Value("${app.post.cache.news_feed.postfix.likes}")
    private String likesPostfix;

    @Value("${app.post.cache.news_feed.postfix.comments}")
    private String commentsPostfix;

    public String build(long id) {
        return buildKey(postIdPrefix, id, null);
    }

    public String buildViewsKey(long postId) {
        return buildKey(postIdPrefix, postId, viewsPostfix);
    }

    public String buildLikesKey(long postId) {
        return buildKey(postIdPrefix, postId, likesPostfix);
    }

    public String buildCommentsCounterKey(long postId) {
        return buildKey(postIdPrefix, postId, commentsPostfix);
    }

    public String getPostKeyFrom(String counterKey) {
        return counterKey.split("/")[0];
    }

    public Long getPostIdFrom(String postKey) {
        return Long.parseLong(postKey.split(":")[1]);
    }

    public String getViewCounterKeyPattern() {
        return postIdPrefix + "*" + viewsPostfix;
    }

    public String getLikeCounterKeyPattern() {
        return postIdPrefix + "*" + likesPostfix;
    }

    public String getCommentCounterKeyPattern() {
        return postIdPrefix + "*" + commentsPostfix;
    }
}
