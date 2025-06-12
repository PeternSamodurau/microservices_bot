// by/spvrent/controller/UpdateController.java
package by.spvrent.controller;

import by.spvrent.service.UpdateProducer;
import by.spvrent.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.spvrent.model.RabbitQueue.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateBotController {

    private final TelegramBotController telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received update is null");
            return;
        }
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Unsupported message type is received: " + update);
        }
    }

    private void distributeMessageByType(Update update) {

        Message message = update.getMessage();

        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocumentMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдерживаемый тип сообщения!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {

        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processPhotoMessage(Update update) {

        updateProducer.producer(PHOTO_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processDocumentMessage(Update update) {

        updateProducer.producer(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);

    }

    private void setFileIsReceivedView(Update update) {

        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен! Обрабатывается ...");
        setView(sendMessage);
    }

    private void processTextMessage(Update update) {

        Message message = update.getMessage();
        String text = message.getText();
        String chatId = message.getChatId().toString();

        log.info("Text message from bot: {} from {}", text, chatId);

        updateProducer.producer(TEXT_MESSAGE_UPDATE, update);

    }
}