package edu.ivanuil.friendalertbot.scheduling;

import edu.ivanuil.friendalertbot.entity.ChatEntity;
import edu.ivanuil.friendalertbot.entity.VisitorEntity;
import edu.ivanuil.friendalertbot.repository.SubscriptionRepository;
import edu.ivanuil.friendalertbot.repository.VisitorRepository;
import edu.ivanuil.friendalertbot.service.TelegramBotService;
import edu.ivanuil.friendalertbot.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshVisitorsSchedule {

    private final VisitorService visitorService;
    private final TelegramBotService botService;

    private final SubscriptionRepository subscriptionRepository;
    private final VisitorRepository visitorRepository;

    @Value("${logging.entering-leaving-visitors-list}")
    private boolean logIncomingLeavingList;

    @Scheduled(fixedRate = 10000)
    public void refreshVisitorsAndSendNotifications() {
        var arr = visitorService.getIncomingAndLeavingVisitors();
        List<VisitorEntity> incomingVisitors = arr[0];
        List<VisitorEntity> leavingVisitors = arr[1];

        if (logIncomingLeavingList) {
            log.info("Incoming visitors: {}", incomingVisitors);
            log.info("Leaving visitors: {}", leavingVisitors);
        }

        Map<ChatEntity, List<VisitorEntity>> incoming = new HashMap<>();
        for (var t : incomingVisitors) {
            for (var sub : subscriptionRepository.findAllBySubscriptionUsername(t.getLogin())) {
                VisitorEntity visitor = visitorRepository.findByLogin(sub.getSubscriptionUsername());
                if (incoming.containsKey(sub.getSubscriberChat()))
                    incoming.get(sub.getSubscriberChat())
                            .add(visitor);
                else
                    incoming.put(sub.getSubscriberChat(), new LinkedList<>(List.of(visitor)));
            }
        }

        Map<ChatEntity, List<String>> leaving = new HashMap<>();
        for (var t : leavingVisitors) {
            for (var sub : subscriptionRepository.findAllBySubscriptionUsername(t.getLogin())) {
                if (leaving.containsKey(sub.getSubscriberChat()))
                    leaving.get(sub.getSubscriberChat())
                            .add(sub.getSubscriptionUsername());
                else
                    leaving.put(sub.getSubscriberChat(), new LinkedList<>(List.of(sub.getSubscriptionUsername())));
            }
        }

        Map<ChatEntity, String> messages = new HashMap<>();
        for (var entry : incoming.entrySet()) {
            var chat = entry.getKey();
            StringBuilder message = new StringBuilder("INCOMING:\n");
            for (var visitor : entry.getValue()) {
                message.append("  ").append(visitor.getLogin()).append("\n");
            }
            if (leaving.containsKey(chat)) {
                message.append("LEAVING:\n");
                for (var visitor : leaving.get(chat)) {
                    message.append("  ").append(visitor).append("\n");
                }
                leaving.remove(chat);
            }
            messages.put(chat, message.toString());
        }
        for (var entry : leaving.entrySet()) {
            var chat = entry.getKey();
            StringBuilder message = new StringBuilder("LEAVING:\n");
            for (var visitor : leaving.get(chat)) {
                message.append("  ").append(visitor).append("\n");
            }
            messages.put(chat, message.toString());
        }

        log.info("Sending notifications to {} users", messages.size());
        botService.notifyUser(messages);
    }

}
