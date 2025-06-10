package by.spvrent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class UpdateProducerImpl implements UpdateProducer{

    private final RabbitTemplate rabbitTemplate;

    public UpdateProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void producer(String rabbitQueue, Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        Long messageId = Long.valueOf(message.getMessageId());
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String chatType = message.getChat().getType();

        log.info("Sending message to queue: {}, messageId: {}, chatId: {}, userId: {}, chatType: {}, text: {}",
                rabbitQueue, messageId, chatId, userId, chatType, text);

        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}
