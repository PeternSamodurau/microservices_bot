package by.spvrent.service.impl;

import by.spvrent.service.interf.ConsumerService;
import by.spvrent.service.interf.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message; // Добавлен импорт Message
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.spvrent.model.RabbitQueue.*;

@Slf4j // Эта аннотация должна быть, чтобы log.info работали
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
        log.info("NODE - ConsumerServiceImpl: --- START PROCESSING TEXT MESSAGE UPDATE ---");
        log.info("NODE - ConsumerServiceImpl: Received raw Update object from RabbitMQ: {}", update);

        if (update != null && update.hasMessage()) {
            Message message = update.getMessage(); // Получаем объект Message
            log.info("NODE - ConsumerServiceImpl: Update has a Message. Message details: {}", message);

            String messageText = message.getText(); // Получаем текст сообщения
            log.info("NODE - ConsumerServiceImpl: Extracted message text from Update: '{}'", messageText);

            if (messageText == null) {
                log.error("NODE - ConsumerServiceImpl: CRITICAL: messageText is NULL after deserialization! This strongly indicates a deserialization problem.");
            } else if (!messageText.startsWith("/")) {
                // Если текст не начинается с '/', то это обычное сообщение, не команда
                log.warn("NODE - ConsumerServiceImpl: messageText does not start with '/', it's treated as regular text: '{}'", messageText);
            } else {
                // Если это команда, попробуем извлечь ее
                String command = messageText.split(" ")[0].split("@")[0].trim();
                log.info("NODE - ConsumerServiceImpl: Parsed command (before MainService): '{}'", command);

                // Дополнительная проверка, если command все еще не "/help", хотя messageText был "/help"
                if (!"/help".equals(command) && "/help".equals(messageText.trim().split(" ")[0].split("@")[0].trim())) {
                    log.error("NODE - ConsumerServiceImpl: PARSING MISMATCH! Original message was likely /help, but parsed command is '{}'", command);
                }
            }
        } else {
            log.warn("NODE - ConsumerServiceImpl: Received null Update object or Update without message after deserialization from RabbitMQ.");
        }

        mainService.processTextMessage(update); // Передаем Update дальше в MainService
        log.info("NODE - ConsumerServiceImpl: --- END PROCESSING TEXT MESSAGE UPDATE ---");
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdate(Update update) {
        log.info("NODE - ConsumerServiceImpl: --- START PROCESSING DOC MESSAGE UPDATE ---");
        log.info("NODE - ConsumerServiceImpl: Received raw Update object from RabbitMQ: {}", update);

        if (update != null && update.hasMessage() && update.getMessage().hasDocument()) {
            log.info("NODE - ConsumerServiceImpl: Document message received: FileId='{}', FileName='{}'",
                    update.getMessage().getDocument().getFileId(), update.getMessage().getDocument().getFileName());
        } else {
            log.warn("NODE - ConsumerServiceImpl: Received null Update or Update without document for DOC_MESSAGE_UPDATE.");
        }

        mainService.processDocMessage(update);
        log.info("NODE - ConsumerServiceImpl: --- END PROCESSING DOC MESSAGE UPDATE ---");
    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdate(Update update) {
        log.info("NODE - ConsumerServiceImpl: --- START PROCESSING PHOTO MESSAGE UPDATE ---");
        log.info("NODE - ConsumerServiceImpl: Received raw Update object from RabbitMQ: {}", update);

        if (update != null && update.hasMessage() && update.getMessage().hasPhoto()) {
            log.info("NODE - ConsumerServiceImpl: Photo message received: PhotoCount='{}'",
                    update.getMessage().getPhoto().size());
        } else {
            log.warn("NODE - ConsumerServiceImpl: Received null Update or Update without photo for PHOTO_MESSAGE_UPDATE.");
        }

        mainService.processPhotoMessage(update);
        log.info("NODE - ConsumerServiceImpl: --- END PROCESSING PHOTO MESSAGE UPDATE ---");
    }
}