package edu.ivanuil.friendalertbot.scheduling;

import edu.ivanuil.friendalertbot.service.bot.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshBotMessagesSchedule {

    private final TelegramBotService botService;

    @Scheduled(fixedDelay = 5_000)
    @Async("jobPool")
    public void refreshBotMessages() {
        botService.refreshUpdates();
    }

}
