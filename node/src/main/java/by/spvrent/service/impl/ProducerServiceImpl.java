package by.spvrent.service.impl;

import by.spvrent.service.interf.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static by.spvrent.model.RabbitQueue.ANSWER_MESSAGE;

@Slf4j
@Service
public class ProducerServiceImpl implements ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public ProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void producerAnswer(SendMessage sendMessage) {
        String chatId = sendMessage.getChatId();
        String text = sendMessage.getText();
        boolean isReplyMarkupPresent = sendMessage.getReplyMarkup() != null;

        log.info("Sending SendMessage to queue: {}, chatId: {}, text: {}, hasReplyMarkup: {}",
                ANSWER_MESSAGE, chatId, text, isReplyMarkupPresent);

        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }
}
