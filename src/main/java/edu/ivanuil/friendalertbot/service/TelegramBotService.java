package edu.ivanuil.friendalertbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import edu.ivanuil.friendalertbot.entity.ChatEntity;
import edu.ivanuil.friendalertbot.entity.SubscriptionEntity;
import edu.ivanuil.friendalertbot.repository.ChatRepository;
import edu.ivanuil.friendalertbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    private final ChatRepository chatRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final School21PlatformBinding school21PlatformBinding;

    @Value("${telegram.bot.token}")
    private String TOKEN;
    private static int lastReadUpdateId = 0;

    private static final String COMMANDS_LIST_MESSAGE = """
            Commands:
                /subscribe [username1] [username2] ... - subscribe to user entering and leaving campus
                /subscribeAll - subscribe to all users currently in campus entering and leaving campus (experimental feature)
            """;

    TelegramBot bot;

    @Scheduled(fixedDelay = 100)
    public void refreshUpdates() {
        if (bot == null)
            bot = new TelegramBot(TOKEN);
        log.info("Checking new messages");
        var updatesResponse = bot.execute(new GetUpdates().offset(lastReadUpdateId + 1));
        var updates = updatesResponse.updates();
        for (var update : updates) {
            lastReadUpdateId = update.updateId();
            processMessage(update.message());
        }
    }

    private void processMessage(Message message) {
        var chatId = message.chat().id();
        if (chatRepository.existsById(chatId)) {
            var chat = chatRepository.findById(chatId).get();

            if (chat.getState() == ChatState.AWAITING_PLATFORM_USERNAME)
                processPlatformUsername(chat, message);
            else if (chat.getState() == ChatState.RUNNING)
                processCommand(chat, message);
        } else {
            processFirstMessage(chatId, message);
        }
    }

    private void processFirstMessage(Long chatId, Message message) {
        if (!message.text().equals("/start"))
            return;
        bot.execute(new SendMessage(chatId,
                "What's you username on the platform (abc@student.21-school.ru format)?"));
        var chat = new ChatEntity(chatId, message.chat().username(),
                null, ChatState.AWAITING_PLATFORM_USERNAME, null);
        chatRepository.save(chat);
    }

    private void processPlatformUsername(ChatEntity chat, Message message) {
        if (checkIfMatchesUsername(message.text())) {
            if (school21PlatformBinding.checkIfUserExists(message.text())) {
                chat.setState(ChatState.RUNNING);
                chat.setPlatformUsername(message.text());
                chatRepository.save(chat);
                bot.execute(new SendMessage(chat.getId(),
                        "All good!"));
                bot.execute(new SendMessage(chat.getId(),
                        COMMANDS_LIST_MESSAGE));
            } else {
                bot.execute(new SendMessage(chat.getId(),
                        "User doesn't exist. Try again"));
            }
        } else {
            bot.execute(new SendMessage(chat.getId(),
                    "Wrong format. Try again"));
        }
    }

    private boolean checkIfMatchesUsername(String username) {
        if (!username.endsWith("@student.21-school.ru"))
            return false;
        if (username.split("@").length != 2)
            return false;
        String login = username.split("@")[0];
        return login.length() == 8 && login.matches("[a-zA-Z]+");
    }

    private void processCommand(ChatEntity chat, Message message) {
        if (message.text().startsWith("/subscribe")) {
            var tokens = message.text().split(" ");
            subscribeToUsers(chat, new LinkedList<>(Arrays.asList(tokens).subList(1, tokens.length)));
        }
    }

    private void subscribeToUsers(ChatEntity chat, List<String> users) {
        Set<SubscriptionEntity> subscriptions = new HashSet<>();
        for (String user : users)
            subscriptions.add(new SubscriptionEntity(null, chat, user));
        subscriptionRepository.saveAll(subscriptions);
    }

    public void notifyUser(ChatEntity chat, String message) {
        bot.execute(new SendMessage(chat.getId(), message));
    }

    public void notifyUser(Map<ChatEntity, String> messages) {
        for (var entry : messages.entrySet())
            notifyUser(entry.getKey(), entry.getValue());
    }

}
