package faang.school.postservice.news_feed.repository.key;

import org.springframework.stereotype.Component;

@Component
public class CacheKey {
    public String buildKey(String prefix, long id, String postfix) {
        return prefix + id + (postfix != null ? postfix : "");
    }
}
