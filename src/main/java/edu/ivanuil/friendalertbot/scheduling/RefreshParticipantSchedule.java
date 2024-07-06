package edu.ivanuil.friendalertbot.scheduling;

import edu.ivanuil.friendalertbot.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshParticipantSchedule {

    private final ParticipantService participantService;

    @Scheduled(fixedDelay = 3_600_000, initialDelay = 3_000) // One every 1 hour
    @Async("jobPool")
    public void refreshParticipantInfo() {
        log.info("Start retrieving participants info");
        participantService.refreshAll();
    }

}
