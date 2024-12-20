package faang.school.postservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ModerationDictionary {

    @Value("${moderation-scheduler.dictionary-path}")
    private String dictionaryPath;

    public boolean isVerified(String postContent) {
        List<String> dictionaryWords = getWords();
        for (String word : dictionaryWords) {
            if(postContent.toLowerCase().contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private List<String> getWords() {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> words = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(new File(dictionaryPath));
            JsonNode wordsNote = rootNode.path("words");

            for (JsonNode wordNote : wordsNote) {
                words.add(wordNote.asText());
            }
        } catch (IOException e) {
            log.error("Dictionary {} could not be parsed", dictionaryPath);
            throw new IllegalStateException("The file could not be parsed", e);
        }
        return words;
    }
}
