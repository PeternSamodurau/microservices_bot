// by/spvrent/controller/TelegramBot.java
package by.spvrent.controller;

import by.spvrent.configuration.BotConfigurationProperties;
import by.spvrent.service.UpdateProducer;
import by.spvrent.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotController extends TelegramLongPollingBot {

    private final BotConfigurationProperties botConfig;
    private final UpdateProducer updateProducer;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
    // при получении каждого обновления неявно вызывается данный метод
    @Override
    public void onUpdateReceived(Update update) {
        log.info("Text received in onUpdateReceived: {}", update.getMessage().getText());
        UpdateBotController updateController = new UpdateBotController(this, new MessageUtils(), updateProducer);
        updateController.processUpdate(update);
    }

    public void sendAnswerMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}