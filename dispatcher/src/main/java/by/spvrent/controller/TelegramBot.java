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
public class TelegramBot extends TelegramLongPollingBot {

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

    @Override
    public void onUpdateReceived(Update update) {
        UpdateController updateController = new UpdateController(this, new MessageUtils(), updateProducer);
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