package by.spvrent.service.impl;

import by.spvrent.service.ConsumerService;
import by.spvrent.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.spvrent.model.RabbitQueue.*;
//В случае с @RabbitListener (и другими подобными аннотациями в Spring, такими как @EventListener, @Scheduled, @RestController для HTTP-запросов), происходит следующее:
//Во время запуска приложения: Spring Boot сканирует все компоненты (классы, помеченные @Service, @Component и т.д.).
//Обнаружение @RabbitListener: Когда Spring находит метод с аннотацией @RabbitListener, он понимает, что этот метод предназначен для обработки сообщений из конкретной очереди RabbitMQ.
//Создание "слушателя" (Message Listener Container): Spring конфигурирует и запускает специальный компонент, называемый "Message Listener Container" (или просто "контейнер слушателя сообщений"). Этот контейнер по сути является постоянно работающим процессом (или несколькими потоками), который:
//Устанавливает соединение с RabbitMQ.
//Подписывается на указанную очередь (например, TEXT_MESSAGE_UPDATE).
//Непрерывно "слушает" эту очередь на предмет новых сообщений.
//При поступлении сообщения:
//Когда в очередь TEXT_MESSAGE_UPDATE приходит новое сообщение (которое было отправлено UpdateProducerImpl методом producer), один из слушателей в контейнере подхватывает его.
//Сообщение из RabbitMQ (которое было сериализовано в JSON с помощью Jackson2JsonMessageConverter) десериализуется обратно в объект Update.
//Затем этот контейнер неявно вызывает метод consumeTextMessageUpdate, передавая ему десериализованный объект Update в качестве аргумента.
//Вы, как разработчик, никогда напрямую не вызываете mainService.consumeTextMessageUpdate(update) или answerConsumer.consumer(sendMessage). Эту работу за вас выполняет фреймворк Spring AMQP (который является частью Spring Ecosystem для работы с RabbitMQ), реагируя на события (поступление сообщения в очередь).
//Почему это сделано "неявно":
//Это фундаментальный принцип асинхронной, событийно-ориентированной архитектуры:
//Разделение ответственности: Отправитель сообщения не знает и не должен знать, кто его получит и как его обработает. Он просто помещает сообщение в очередь.
//Гибкость и масштабируемость: Если вы захотите добавить больше обработчиков (масштабировать), вы просто запустите еще один экземпляр вашего приложения, и Spring в нем автоматически создаст еще один слушатель. RabbitMQ сам распределит нагрузку между ними.
//Отказоустойчивость: Если обработчик упадет, сообщение останется в очереди и может быть обработано другим слушателем или после перезапуска.
//Неблокирующая работа: Основной поток, который принял сообщение от Telegram (UpdateBotController), быстро отправляет его в очередь и может сразу же освободиться для обработки следующих входящих сообщений, не дожидаясь завершения всей сложной бизнес-логики.
//Таким образом, "неявный вызов" является ключевым элементом для построения мощных, масштабируемых и отказоустойчивых систем на основе очередей сообщений.

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdate(Update update) {
    log.info("NODE: Text message is received ");
    mainService.processTextMessage(update);
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdate(Update update) {

        log.info("NODE: Doc message is received ");
        mainService.processDocMessage(update);
    }

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdate(Update update) {

        log.info("NODE: Photo message is received ");
        mainService.processPhotoMessage(update);
    }
}
