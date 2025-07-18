package by.spvrent.service.interf;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {

    void consumeTextMessageUpdate(Update update);
    void consumeDocMessageUpdate(Update update);
    void consumePhotoMessageUpdate(Update update);
}
