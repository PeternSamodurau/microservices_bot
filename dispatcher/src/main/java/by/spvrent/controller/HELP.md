После успешного старта приложения и инициализации всех компонентов, включая:
- Загрузку конфигурации из application.properties (BotConfigurationProperties).
 - Регистрацию бота в Telegram API (BotInitializer).
- Настройку подключения к RabbitMQ (RabbitConfiguration).
- Создание TelegramBotsApi (TelegramBotsApiConfig).

Бот переходит в состояние готовности и начинает слушать обновления от Telegram через механизм Long Polling, 
который, используется за кулисами DefaultBotSession. 
Это означает, что он ожидает, когда пользователи начнут отправлять ему сообщения или использовать команды. 

После отправки сообщения "Hello from bot", именно класс TelegramBotController (by/spvrent/controller/TelegramBotController.java), 
который расширяет TelegramLongPollingBot, первым получает обновление от Telegram API.

Давайте разберем код:
- @Component: Эта аннотация говорит Spring, что это компонент, которым нужно управлять. 
             Spring создаст экземпляр этого класса и будет внедрять его зависимости.
- @RequiredArgsConstructor: Аннотация Lombok, автоматически генерирует конструктор с одним параметром для каждого поля, 
  помеченного как final или @NonNull. В данном случае, это botConfig и updateProducer.
- @Slf4j: Аннотация Lombok, автоматически создает logger для этого класса.
- extends TelegramLongPollingBot: Это ключевой момент. Этот класс является Telegram ботом, 
  использующим Long Polling для получения обновлений.
- getBotUsername() и getBotToken(): Эти методы переопределяются из TelegramLongPollingBot и возвращают имя и токен бота, 
  которые загружаются из application.properties.
- onUpdateReceived(Update update): Это метод, который вызывается каждый раз, когда бот получает новое обновление от Telegram.
  Внутри этого метода создается экземпляр UpdateController.
  
В итоге:
После запуска приложения, TelegramLongPollingBot устанавливает и поддерживает постоянный канал связи с Telegram API. 
Это реализуется через механизм Long Polling
Когда вы отправляете сообщение боту, Telegram API отправляет это сообщение в виде Update на ваш сервер.
TelegramLongPollingBot получает этот Update.
Метод onUpdateReceived(Update update) в TelegramBotController вызывается.
В onUpdateReceived создается UpdateController для непосредственной реакции на обновление и вызывается его метод processUpdate для обработки обновления.
Таким образом, TelegramBotController является точкой входа для всех входящих сообщений и событий.
Он делегирует обработку обновления классу UpdateController.


UpdateController - это класс, который непосредственно обрабатывает обновления, полученные от Telegram API. 

Структура класса UpdateController:

- @Slf4j: Добавляет логгер для записи информации о работе класса.
- @Component: Помечает класс как Spring-компонент, позволяя Spring управлять его жизненным циклом.
- @RequiredArgsConstructor: Аннотация Lombok, генерирует конструктор, принимающий все final поля в качестве аргументов. 
  Это обеспечивает внедрение зависимостей (TelegramBotController, MessageUtils, UpdateProducer).
Поля:
- telegramBot: Экземпляр TelegramBotController (нашего основного класса бота). Используется для отправки ответов пользователю.
- messageUtils: Экземпляр MessageUtils. Используется для генерации SendMessage объектов (формирование ответов).
- updateProducer: Экземпляр UpdateProducer. Используется для отправки информации об обновлении в систему обмена сообщениями (RabbitMQ).

Методы UpdateController:
- processUpdate(Update update):
Основной метод для обработки обновлений.
Проверяет, что обновление не null.
Проверяет, что в обновлении есть сообщение (update.getMessage() != null).
Вызывает метод distributeMessageByType(update) для дальнейшей обработки сообщения в зависимости от его типа.
Логирует ошибку, если тип сообщения не поддерживается.
- distributeMessageByType(Update update):
Определяет тип сообщения (текст, документ, фото) и вызывает соответствующий метод обработки.
- request.hasText(): Проверяет, содержит ли сообщение текст.
- request.getDocument() != null: Проверяет, содержит ли сообщение документ.
- request.getPhoto() != null: Проверяет, содержит ли сообщение фотографию.
Вызывает setUnsupportedMessageTypeView(update), если тип сообщения не поддерживается.
- setUnsupportedMessageTypeView(Update update):
Формирует сообщение об ошибке "Неподдерживаемый тип сообщения!" с помощью messageUtils.generateSendMessageWithText().

Вызывает setView(sendMessage) для отправки сообщения пользователю.
- setView(SendMessage sendMessage):
Отправляет сформированное сообщение пользователю с помощью telegramBot.sendAnswerMessage(sendMessage). 
Это важный метод, который связывает UpdateController с TelegramBotController и позволяет боту отправлять ответы.
- processPhotoMessage(Update update):
Отправляет информацию об обновлении (фотографии) в очередь сообщений PHOTO_MESSAGE_UPDATE с помощью updateProducer.producer().
Вызывает setFileIsReceivedView(update) для отправки пользователю уведомления о получении файла.
processDocumentMessage(Update update):
Аналогично processPhotoMessage(Update update), но для документов и очереди DOC_MESSAGE_UPDATE.
- setFileIsReceivedView(Update update):
Формирует сообщение "Файл получен! Обрабатывается ..." с помощью messageUtils.generateSendMessageWithText().
Вызывает setView(sendMessage) для отправки сообщения пользователю.
- processTextMessage(Update update):
Извлекает текст сообщения и ID чата из update.
Логирует полученное сообщение.
Отправляет информацию об обновлении (текстовом сообщении) в очередь сообщений TEXT_MESSAGE_UPDATE с помощью updateProducer.producer().

- Функциональность и взаимодействие:
TelegramBotController получает Update от Telegram API.
TelegramBotController создает экземпляр UpdateController и передает ему Update, а также необходимые зависимости.
UpdateController анализирует Update и определяет тип сообщения.
В зависимости от типа сообщения, вызывается соответствующий метод обработки (processTextMessage, processDocumentMessage, processPhotoMessage).
Эти методы отправляют информацию об обновлении в соответствующие очереди RabbitMQ через updateProducer.
UpdateController также формирует ответы пользователю (например, уведомления о получении файла) и отправляет их через telegramBot.sendAnswerMessage().

Ключевые моменты:
UpdateController отвечает за маршрутизацию и начальную обработку входящих обновлений.
Он использует MessageUtils для упрощения формирования ответов.
Он использует UpdateProducer для отправки информации об обновлениях в RabbitMQ, что позволяет другим микросервисам обрабатывать их асинхронно.
Он использует TelegramBotController для отправки ответов пользователю.
Таким образом, UpdateController является центральным компонентом, который принимает обновления от Telegram, определяет их тип, и запускает соответствующие процессы обработки.
