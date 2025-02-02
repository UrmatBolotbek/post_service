package faang.school.postservice.controller.feed;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.service.feed.FeedHeaterService;
import faang.school.postservice.news_feed.service.feed.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
public class FeedController {
    private final UserContext userContext;
    private final FeedService feedService;
    private final FeedHeaterService feedHeaterService;

    @GetMapping
    public List<PostCache> getPosts(@RequestParam(name = "offset") Long offset,
                                    @RequestParam(name = "limit") Long limit) {

        return feedService.getFeed(userContext.getUserId(), offset, limit);
    }

    @GetMapping("/heat")
    public void heatUsersFeed() {
        feedHeaterService.heatUsersFeeds();
    }
}