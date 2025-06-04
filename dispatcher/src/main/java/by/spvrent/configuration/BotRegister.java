package by.spvrent.configuration;

import by.spvrent.controller.TelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotRegister {

    private final TelegramBotsApi telegramBotsApi;
    private final TelegramBot telegramBot;

    @PostConstruct // Автоматически вызывается после создания Bean'а
    public void registerBot() {
        try {
            telegramBotsApi.registerBot(telegramBot);
            log.info("Bot successfully registered!");
        } catch (TelegramApiException e) {
            log.error("Failed to register bot", e);
        }
    }
}
