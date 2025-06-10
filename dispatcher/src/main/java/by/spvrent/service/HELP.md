   Создадим карту вызовов методов в порядке их выполнения при получении и обработке обновления от Telegram API.
   
1. Telegram API -> TelegramBotController.onUpdateReceived(Update update)
   Вызывается неявно фреймворком Telegram Bots (Long Polling).
   Получает объект Update от Telegram API.

2. TelegramBotController.onUpdateReceived(Update update) -> UpdateBotController.processUpdate(Update update)
   Создает экземпляр UpdateBotController:
   UpdateBotController updateController = new UpdateBotController(this, new MessageUtils(), updateProducer);
   this: Ссылка на экземпляр TelegramBotController.
   new MessageUtils(): Новый экземпляр класса MessageUtils.
   updateProducer: Внедренный экземпляр класса, реализующего UpdateProducer.
   Вызывает метод processUpdate:
   updateController.processUpdate(update);

3. UpdateBotController.processUpdate(Update update) -> UpdateBotController.distributeMessageByType(Update update) (условно)
   Условие: if (update.getMessage() != null)
   Проверяет, что обновление содержит сообщение.

4. UpdateBotController.distributeMessageByType(Update update) -> Один из следующих методов (условно):
   Условие: if (request.hasText()) -> UpdateBotController.processTextMessage(Update update)
   Условие: else if (request.getDocument() != null) -> UpdateBotController.processDocumentMessage(Update update)
   Условие: else if (request.getPhoto() != null) -> UpdateBotController.processPhotoMessage(Update update)
   Иначе: -> UpdateBotController.setUnsupportedMessageTypeView(Update update)

5. UpdateBotController.processTextMessage(Update update) -> UpdateProducer.producer(TEXT_MESSAGE_UPDATE, update)
   Извлекает текст сообщения и ID чата.
   Вызывает метод producer у updateProducer, передавая имя очереди TEXT_MESSAGE_UPDATE и объект update.
   
   UpdateBotController.processDocumentMessage(Update update) -> UpdateProducer.producer(DOC_MESSAGE_UPDATE, update) -> UpdateBotController.setFileIsReceivedView(Update update)
   Вызывает метод producer у updateProducer, передавая имя очереди DOC_MESSAGE_UPDATE и объект update.
   Вызывает setFileIsReceivedView для отправки подтверждения пользователю.
   
   UpdateBotController.processPhotoMessage(Update update) -> UpdateProducer.producer(PHOTO_MESSAGE_UPDATE, update) -> UpdateBotController.setFileIsReceivedView(Update update)
   Вызывает метод producer у updateProducer, передавая имя очереди PHOTO_MESSAGE_UPDATE и объект update.
   Вызывает setFileIsReceivedView для отправки подтверждения пользователю.
   
   UpdateBotController.setUnsupportedMessageTypeView(Update update) -> MessageUtils.generateSendMessageWithText(Update update, String text) -> UpdateBotController.setView(SendMessage sendMessage)
   Формирует сообщение об ошибке.
   Вызывает setView для отправки сообщения пользователю.

6. UpdateProducer.producer(String rabbitQueue, Update update) -> RabbitTemplate.convertAndSend(rabbitQueue, update)
   Вызывается из processTextMessage, processDocumentMessage, processPhotoMessage.
   Отправляет объект Update в указанную очередь RabbitMQ.

7. UpdateBotController.setFileIsReceivedView(Update update) -> MessageUtils.generateSendMessageWithText(Update update, String text) -> UpdateBotController.setView(SendMessage sendMessage)
   Вызывается из processDocumentMessage, processPhotoMessage.
   Формирует сообщение "Файл получен! Обрабатывается ...".
   Вызывает setView для отправки сообщения пользователю.

8. UpdateBotController.setView(SendMessage sendMessage) -> TelegramBotController.sendAnswerMessage(SendMessage sendMessage)
   Вызывается из setUnsupportedMessageTypeView, setFileIsReceivedView, и возможно из других мест (если микросервисы возвращают SendMessage через RabbitMQ).
   Отправляет объект SendMessage пользователю через Telegram API.

9. TelegramBotController.sendAnswerMessage(SendMessage sendMessage) -> TelegramBotController.execute(SendMessage sendMessage)
   Отправляет сообщение в Telegram API.
   Возможна обработка исключений TelegramApiException.

10. (Асинхронно, в другом микросервисе) RabbitMQ -> AnswerConsumerImpl.consumer(SendMessage sendMessage)
    Вызывается неявно Spring AMQP, когда сообщение поступает в очередь ANSWER_MESSAGE.
    Получает объект SendMessage из очереди ANSWER_MESSAGE.

11. AnswerConsumerImpl.consumer(SendMessage sendMessage) -> UpdateBotController.setView(SendMessage sendMessage)
    Передает SendMessage в UpdateBotController для отправки пользователю.

12. (Повторение) UpdateBotController.setView(SendMessage sendMessage) -> TelegramBotController.sendAnswerMessage(SendMessage sendMessage) -> TelegramBotController.execute(SendMessage sendMessage)
    Отправляет сообщение в Telegram API.

    Ключевые моменты:
    Карта показывает последовательность вызовов, начиная с получения обновления и заканчивая отправкой ответа.
    UpdateProducer и RabbitMQ обеспечивают асинхронную связь между компонентами.
    Условные переходы зависят от типа полученного сообщения.
    Цикл завершается отправкой сообщения пользователю через Telegram API.
    Эта карта вызовов дает четкое представление о том, как различные компоненты взаимодействуют для обработки сообщений в Telegram боте.