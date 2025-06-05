# Telegram Bot Dispatcher Application

Этот проект представляет собой простое Spring Boot приложение, которое реализует Telegram-бота. Бот получает сообщения от пользователей и может быть настроен для выполнения различных действий в ответ.

## Описание

Приложение состоит из нескольких ключевых компонентов:

*   **`BotConfigurationProperties`**: Класс конфигурации, который загружает имя и токен бота из файла `application.properties`. ([Ссылка на BotConfigurationProperties.java](C:\Users\user\IdeaProjects\microservices_bot\dispatcher\src\main\java\by\spvrent\configuration\BotConfigurationProperties.java))
*   **`TelegramBotsApiConfig`**: Класс конфигурации, который создает бин `TelegramBotsApi`. ([Ссылка на TelegramBotsApiConfig.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/configuration/TelegramBotsApiConfig.java))
*   **`BotInitializer`**: Класс, который регистрирует Telegram-бота в API после запуска приложения. ([Ссылка на BotInitializer.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/configuration/BotInitializer.java))
*   **`TelegramBot`**: Основной класс бота, который получает обновления от Telegram и передает их на обработку. ([Ссылка на TelegramBot.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/controller/TelegramBot.java))
*   **`UpdateController`**: Класс, который обрабатывает входящие обновления и определяет, как на них реагировать. ([Ссылка на UpdateController.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/controller/UpdateController.java))
*   **`DispatcherApplication`**: Главный класс приложения Spring Boot. ([Ссылка на DispatcherApplication.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/DispatcherApplication.java))

## Архитектура

1.  **Получение обновлений:** Пользователь отправляет сообщение боту в Telegram.
2.  **Обработка обновлений:** Telegram отправляет обновление (объект `Update`) в приложение.
3.  **Регистрация бота:** `BotInitializer` регистрирует бота в Telegram API после запуска приложения.
4.  **Диспетчеризация обновлений:** Класс `TelegramBot` получает объект `Update` и передает его в `UpdateController` для обработки.
5.  **Обработка обновлений:** Класс `UpdateController` принимает объект `Update` и может быть расширен для выполнения различных действий, включая отправку ответов.

## Компоненты

### `BotConfigurationProperties`

Класс `BotConfigurationProperties` отвечает за загрузку конфигурации бота из файла `application.properties`. ([Ссылка на BotConfigurationProperties.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/configuration/BotConfigurationProperties.java))

*   `@Configuration`: Указывает, что это класс конфигурации Spring.
*   `@PropertySource("classpath:application.properties")`: Указывает, что свойства нужно загружать из файла `application.properties`, находящегося в classpath.
*   `@Value("${bot.name}")` и `@Value("${bot.token}")`: Аннотации `@Value` используются для внедрения значений свойств `bot.name` и `botToken` соответственно.
*   `@Data`: Аннотация Lombok, автоматически генерирующая геттеры, сеттеры, `equals()`, `hashCode()` и `toString()`.

### `TelegramBotsApiConfig`

Класс `TelegramBotsApiConfig` отвечает за создание бина `TelegramBotsApi`. ([Ссылка на TelegramBotsApiConfig.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/configuration/TelegramBotsApiConfig.java))

*   `@Configuration`: Указывает, что это класс конфигурации Spring.
*   `@Bean`: Аннотация `@Bean` используется для создания бина `TelegramBotsApi`.

### `BotInitializer`

Класс `BotInitializer` отвечает за регистрацию бота в Telegram API после запуска приложения. ([Ссылка на BotInitializer.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/configuration/BotInitializer.java))

*   `@Configuration`: Указывает, что это класс конфигурации Spring.
*   `@Autowired private TelegramBot bot`: Внедрение зависимости `TelegramBot`.
*   `@Autowired private TelegramBotsApi telegramBotsApi`: Внедрение зависимости `TelegramBotsApi`.
*   `@PostConstruct registerBot()`: Регистрация бота после инициализации бина.

### `TelegramBot`

Класс `TelegramBot` является основным классом бота. Он получает обновления от Telegram и передает их на обработку. ([Ссылка на TelegramBot.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/controller/TelegramBot.java))

*   `@Slf4j`: Аннотация Lombok, добавляющая logger.
*   `@Component`: Указывает, что это компонент Spring.
*   `@RequiredArgsConstructor`: Аннотация Lombok, генерирующая конструктор с аргументами для всех `final` полей.
*   `TelegramLongPollingBot`: Суперкласс для Telegram-ботов, использующих Long Polling.
*   `getBotUsername()`: Возвращает имя бота, полученное из `BotConfig`.
*   `getBotToken()`: Возвращает токен бота, полученный из `BotConfig`.
*   `onUpdateReceived(Update update)`: Метод, который вызывается при получении нового обновления от Telegram. Здесь происходит создание `UpdateController` и передача ему управления.
*   `sendAnswerMessage(SendMessage sendMessage)`: Метод для отправки сообщения в Telegram.

### `UpdateController`

Класс `UpdateController` отвечает за обработку входящих обновлений. ([Ссылка на UpdateController.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/controller/UpdateController.java))

*   `@Slf4j`: Аннотация Lombok, добавляющая logger.
*   `@Component`: Указывает, что это компонент Spring.
*   `@RequiredArgsConstructor`: Аннотация Lombok, генерирующая конструктор с аргументами для всех `final` полей.
*   `processUpdate(Update update)`: Метод, который вызывается для обработки обновления.
    *   Проверяет, что обновление не `null`.
    *   Вызывает `distributeMessageByType()` для дальнейшей обработки, если обновление содержит сообщение.
    *   В противном случае, логирует ошибку.
*   `distributeMessageByType(Update update)`: Метод, который определяет тип сообщения (текст, документ, фото и т.д.) и вызывает соответствующий метод обработки.
*   `processTextMessage(Update update)`: Метод, который обрабатывает текстовые сообщения.
    *   Извлекает текст сообщения и ID чата.
    *   Логирует полученное сообщение.
    *   Создает объект `SendMessage` с текстом "Hello from bot!!!" (используя `MessageUtils`).
    *   Вызывает `setView()` для отправки ответа.
*   `setUnsupportedMessageTypeView(Update update)`: Метод, который вызывается, если тип сообщения не поддерживается. Создает и отправляет сообщение об ошибке.
*   `setView(SendMessage sendMessage)`: Метод, который отправляет сообщение в Telegram, используя `telegramBot.sendAnswerMessage()`.
*   `processPhotoMessage(Update update)`: Метод для обработки фото (в текущей версии не реализован).
*   `processDocumentMessage(Update update)`: Метод для обработки документов (в текущей версии не реализован).

### `DispatcherApplication`

Класс `DispatcherApplication` является главным классом приложения Spring Boot. ([Ссылка на DispatcherApplication.java](https://github.com/your-username/your-repo/blob/main/src/main/java/by/spvrent/DispatcherApplication.java))

*   `@SpringBootApplication`: Аннотация, объединяющая `@Configuration`, `@EnableAutoConfiguration` и `@ComponentScan`.
*   `main(String[] args)`: Главный метод приложения.

## Зависимости

В проекте используются следующие зависимости:

*   `org.springframework.boot:spring-boot-starter-web`
*   `org.telegram:telegrambots-spring-boot-starter`
*   `org.projectlombok:lombok`
*   `org.slf4j:slf4j-api`
*   `jakarta.annotation:jakarta.annotation-api`

## Как запустить приложение

1.  **Клонируйте репозиторий:** `git clone [URL репозитория]`
2.  **Перейдите в директорию проекта:** `cd [директория проекта]`
3.  **Создайте файл `application.properties` в `src/main/resources`:**

    ```properties
    bot.name=YourBotName
    bot.token=YourBotToken
    ```

    Замените `YourBotName` на имя вашего бота, а `YourBotToken` на токен вашего бота, полученный от BotFather в Telegram.
4.  **Запустите приложение:** `./gradlew bootRun` (или запустите класс `DispatcherApplication` в вашей IDE).

## Как протестировать приложение

1.  Запустите приложение, как описано выше.
2.  Найдите своего бота в Telegram по имени пользователя.
3.  Отправьте боту сообщение.
4.  Проверьте консоль приложения. Вы должны увидеть логи о получении сообщения (если вы добавили логику в `UpdateController`).

## Расширение функциональности

Этот проект предоставляет базовый шаблон для создания Telegram-ботов. Вы можете расширить его функциональность, добавив:

*   Обработку других типов сообщений (документы, фото, команды и т.д.).
*   Интеграцию с другими сервисами и API.
*   Использование клавиатур и других интерактивных элементов Telegram.
*   Логику обработки сообщений и отправки ответов в `UpdateController`.

## Заключение

Этот README предоставляет подробное описание структуры и функциональности проекта, а также ссылки на исходный код. Надеюсь, это поможет вам понять, как работает приложение, и начать создавать своих собственных Telegram-ботов!

**Пожалуйста, не забудьте заменить `your-username` и `your-repo` на ваши реальные данные.**

Теперь в README должно быть полное описание класса `UpdateController` и всех его методов.