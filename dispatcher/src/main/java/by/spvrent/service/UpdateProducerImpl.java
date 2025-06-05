package by.spvrent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class UpdateProducerImpl implements UpdateProducer{
    @Override
    public void producer(String rabbitQueue, Update update) {
        log.debug(update.getMessage().getText());
    }
}
