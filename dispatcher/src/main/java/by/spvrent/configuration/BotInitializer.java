package by.spvrent.configuration;

import by.spvrent.controller.TelegramBotController;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Configuration
public class BotInitializer {

    @Autowired
    private TelegramBotController bot;

    @Autowired
    private TelegramBotsApi telegramBotsApi;

    @PostConstruct
    public void registerBot() {
        try {
            telegramBotsApi.registerBot(bot);
            log.info("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            log.error("Error registering Telegram bot", e);
        }
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}