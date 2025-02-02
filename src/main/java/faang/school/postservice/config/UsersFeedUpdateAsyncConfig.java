package faang.school.postservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class UsersFeedUpdateAsyncConfig {
    @Value("${app.async-config.users_feed_update.core_pool_size}")
    private int corePoolSize;

    @Value("${app.async-config.users_feed_update.max_pool_size}")
    private int maxPoolSize;

    @Value("${app.async-config.users_feed_update.queue_capacity}")
    private int queueCapacity;

    @Value("${app.async-config.users_feed_update.thread_mane_prefix}")
    private String threadNamePrefix;

    @Bean
    public Executor usersFeedsUpdatePool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        return executor;
    }
}
