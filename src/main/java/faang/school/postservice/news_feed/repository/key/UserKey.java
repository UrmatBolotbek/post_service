package faang.school.postservice.news_feed.repository.key;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UserKey extends CacheKey {
    @Value("${app.post.cache.news_feed.prefix.user_id}")
    private String userIdPrefix;

    @Value("${app.post.cache.news_feed.prefix.feed_prefix}")
    private String feedPrefix;

    public String build(long userId) {
        return buildKey(userIdPrefix, userId, null);
    }

    public String buildFeedKey(long userId) {
        return buildKey(feedPrefix + userIdPrefix, userId, null);
    }
}
