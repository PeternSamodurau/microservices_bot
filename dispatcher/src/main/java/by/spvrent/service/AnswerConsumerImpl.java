package by.spvrent.service;

import by.spvrent.controller.UpdateBotController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static by.spvrent.model.RabbitQueue.ANSWER_MESSAGE;
@Slf4j
@Service
public class AnswerConsumerImpl implements AnswerConsumer{

    private final UpdateBotController updateController;

    public AnswerConsumerImpl(UpdateBotController updateController) {
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consumer(SendMessage sendMessage) {

        String chatId = sendMessage.getChatId();
        String text = sendMessage.getText();
        boolean isReplyMarkupPresent = sendMessage.getReplyMarkup() != null;

        log.info("Received SendMessage from queue: {}, chatId: {}, text: {}, hasReplyMarkup: {}",
                ANSWER_MESSAGE, chatId, text, isReplyMarkupPresent);

        updateController.setView(sendMessage);
    }
}
