package faang.school.postservice.config.api;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spelling")
public class SpellingConfig {

    private String key;
    private String endpoint;
    private String host;
    private String content;

}
