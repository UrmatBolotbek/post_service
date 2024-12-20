package faang.school.postservice.scheduler;

import faang.school.postservice.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommenterBanner {

    private final CommentService commentService;

    @Scheduled(cron = "${moderation-scheduler.cron}")
    public void commenterBanner() {
        commentService.commenterBanner();
    }

}
