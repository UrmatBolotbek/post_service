package faang.school.postservice.service.news_feed.cache;

public enum RedisTransactionResult {
    SUCCESS,
    NOT_FOUND,
    LOCK_EXCEPTION,
    FAILURE
}
