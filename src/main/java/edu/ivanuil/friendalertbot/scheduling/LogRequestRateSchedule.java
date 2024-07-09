package edu.ivanuil.friendalertbot.scheduling;

import edu.ivanuil.friendalertbot.service.School21PlatformBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogRequestRateSchedule {

    private final School21PlatformBinding platformBinding;

    @Scheduled(fixedDelay = 60_000)
    @Async("jobPool")
    public void refreshBotMessages() {
        log.info("S21 platform request rate: {} per second",
                String.format("%.2f", platformBinding.getRequestRatePerSecondAndReset()));
    }

}
