package by.spvrent.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void producer(String rabbitQueue, Update update);
}
