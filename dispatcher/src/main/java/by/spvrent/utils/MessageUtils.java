package by.spvrent.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageUtils {

    public SendMessage generateSendMessageWithText(Update update, String text){

        Message getMessage = update.getMessage();

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(getMessage.getChatId().toString());

        sendMessage.setText(text);

        return sendMessage;
    }
}
