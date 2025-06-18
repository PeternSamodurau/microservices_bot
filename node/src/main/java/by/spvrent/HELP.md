Этот сервис явно является **"узлом обработки" (Node Service)** для Telegram-бота, 
отвечающим за внутреннюю логику и взаимодействие с данными.

---

# Описание микросервиса: Telegram Bot Processing Node (Node Service)

## 1. Введение

Данный документ описывает микросервис `telegram-bot-node-service` (или просто "Node"), 
который является ключевым компонентом архитектуры Telegram-бота. 
Его основная задача — **асинхронная обработка входящих обновлений** от Telegram (текстовые сообщения, документы, фотографии) 
и взаимодействие с уровнем данных для сохранения информации, 
а также подготовка и отправка ответов обратно в основное приложение бота (через очередь сообщений).

Этот сервис разработан с использованием **Spring Boot** и ориентирован на работу в распределенной системе,
используя **RabbitMQ** для межсервисной коммуникации.

## 2. Ключевые Ответственности

*   **Потребление обновлений:** Получение различных типов сообщений (текст, документ, фото) из очередей RabbitMQ.
*   **Обработка текстовых команд:** Разбор и выполнение предопределенных команд бота (например, `/start`, `/help`, `/cancel`).
*   **Управление состоянием пользователя:** Отслеживание и изменение текущего состояния пользователя (например, `BASIC_STATE`, `WAIT_FOR_EMAIL_STATE`) для поддержки диалогов.
*   **Сохранение данных пользователя:** Регистрация новых пользователей и обновление существующих данных в базе.
*   **Обработка файлов:** Загрузка документов и фотографий, отправленных пользователями, из Telegram API и сохранение их в базе данных.
*   **Генерация ответов:** Формирование текстовых ответов для пользователей и отправка их обратно в очередь RabbitMQ для дальнейшей отправки ботом.
*   **Персистентность данных:** Взаимодействие с базой данных для хранения `AppUser`, `AppDocument`, `AppBinaryContent` и `RawData`.

## 3. Архитектура и Взаимодействие

Микросервис Node работает как **потребитель (Consumer)** и **производитель (Producer)** сообщений в распределенной системе:

*   **Входящие сообщения:** Бот-диспетчер (или Gateway-сервис) получает обновления от Telegram API и публикует их в соответствующие очереди RabbitMQ (например, `text_message_update`, `doc_message_update`, `photo_message_update`).
*   **Обработка:** Микросервис Node прослушивает эти очереди, потребляет сообщения, обрабатывает их бизнес-логикой и взаимодействует с базой данных.
*   **Исходящие сообщения:** После обработки Node формирует ответные сообщения (`SendMessage`) и публикует их в общую очередь RabbitMQ (`answer_message`), которую затем потребляет бот-диспетчер для отправки пользователю.

**Схема взаимодействия:**

```
Telegram -> Bot Gateway (Dispatcher)
  |
  V
RabbitMQ Queues (text_message_update, doc_message_update, photo_message_update)
  |
  V
[ Telegram Bot Processing Node (Этот сервис) ]
  |   |   |
  |   |   V  (Telegram Bot API для загрузки файлов)
  |   V  База Данных (AppUser, AppDocument, AppBinaryContent, RawData)
  V
RabbitMQ Queue (answer_message)
  |
  V
Bot Gateway (Dispatcher) -> Telegram
```

## 4. Основные Компоненты (Классы и Интерфейсы)

### 4.1. Главные Сервисы и Обработчики

*   **`by.spvrent.NodeApplication`**:
    *   Основной класс Spring Boot приложения. Точка входа для запуска микросервиса.
    *   Содержит простой логгер, который сообщает URL, на котором запущено приложение.

*   **`by.spvrent.service.interf.ConsumerService`**:
    *   Интерфейс для классов, отвечающих за потребление сообщений из RabbitMQ.
    *   Методы: `consumeTextMessageUpdate`, `consumeDocMessageUpdate`, `consumePhotoMessageUpdate`.

*   **`by.spvrent.service.impl.ConsumerServiceImpl`**:
    *   Имплементация `ConsumerService`.
    *   Использует аннотации `@RabbitListener` для прослушивания предопределенных очередей RabbitMQ (`TEXT_MESSAGE_UPDATE`, `DOC_MESSAGE_UPDATE`, `PHOTO_MESSAGE_UPDATE`).
    *   Делегирует обработку сообщений сервису `MainService`.

*   **`by.spvrent.service.interf.MainService`**:
    *   Интерфейс для основной бизнес-логики обработки сообщений.
    *   Методы: `processTextMessage`, `processDocMessage`, `processPhotoMessage`.

*   **`by.spvrent.service.impl.MainServiceImpl`**:
    *   Имплементация `MainService`. Центральный оркестратор обработки обновлений.
    *   **Логика `processTextMessage`**:
        *   Сохраняет сырые данные (`RawData`).
        *   Определяет или создает пользователя (`AppUser`).
        *   Обрабатывает команды (`/cancel`, `/registration`, `/help`, `/start`).
        *   Поддерживает пользовательские состояния (например, `WAIT_FOR_EMAIL_STATE` для ввода email).
        *   Формирует и отправляет ответы пользователю через `ProducerService`.
    *   **Логика `processDocMessage` и `processPhotoMessage`**:
        *   Сохраняет сырые данные.
        *   Проверяет активность пользователя и его текущее состояние перед разрешением загрузки контента.
        *   Для документов делегирует загрузку и сохранение `FileService`.
        *   Для фото содержит `//TODO` для будущей реализации сохранения.
        *   Отправляет подтверждение пользователю.

*   **`by.spvrent.service.interf.FileService`**:
    *   Интерфейс для обработки и сохранения файлов.
    *   Метод: `processDoc` (для документов).

*   **`by.spvrent.service.impl.FileServiceImpl`**:
    *   Имплементация `FileService`.
    *   Отвечает за загрузку файлов из Telegram API.
    *   Использует `RestTemplate` для выполнения HTTP-запросов к Telegram API (`getFile` для получения `file_path` и `downloadFile` для загрузки байтов файла).
    *   Сохраняет бинарное содержимое файла (`AppBinaryContent`) и метаданные (`AppDocument`) в базе данных.
    *   Обрабатывает возможные ошибки загрузки файлов (`UploadFileException`).
    *   Получает `bot.token`, `service.file_info.uri`, `service.file_storage.uri` из конфигурации.

*   **`by.spvrent.service.interf.ProducerService`**:
    *   Интерфейс для классов, отвечающих за отправку ответных сообщений в RabbitMQ.
    *   Метод: `producerAnswer`.

*   **`by.spvrent.service.impl.ProducerServiceImpl`**:
    *   Имплементация `ProducerService`.
    *   Использует `RabbitTemplate` для отправки объектов `SendMessage` в очередь `ANSWER_MESSAGE`.

### 4.2. Сущности и DAO (Уровень Данных)

*   **`by.spvrent.entity.AppUser`**:
    *   Сущность, представляющая пользователя Telegram.
    *   Хранит `telegramUserId`, `firstName`, `lastName`, `userName`, `email`, `isActive`, `firstLoginDate` и `appUserState`.
    *   `appUserState` (тип `AppUserState`) — текущее состояние пользователя в рамках диалога с ботом.

*   **`by.spvrent.entity.AppUserState`**:
    *   Enum, определяющий возможные состояния пользователя (`BASIC_STATE`, `WAIT_FOR_EMAIL_STATE`).

*   **`by.spvrent.entity.AppDocument`**:
    *   Сущность, представляющая метаданные загруженного документа.
    *   Хранит `telegramFileId`, `docName`, `mimeType`, `fileSize`.
    *   Имеет связь `@OneToOne` с `AppBinaryContent` для хранения самих байтов файла.

*   **`by.spvrent.entity.AppBinaryContent`**:
    *   Сущность, хранящая бинарное содержимое файла (`fileAsArrayOfBytes`).

*   **`by.spvrent.entity.RawData`**:
    *   Сущность для сохранения "сырых" `Update` объектов, полученных от Telegram. Используется для отладки или последующего анализа.

*   **`by.spvrent.dao.*DAO` (AppUserDAO, AppDocumentDAO, AppBinaryContentDAO, RawDataDAO)**:
    *   Интерфейсы репозиториев Spring Data JPA.
    *   Предоставляют стандартные методы CRUD для работы с соответствующими сущностями в базе данных.
    *   `AppUserDAO` имеет дополнительный метод `findAppUserByTelegramUserId` для поиска пользователя по его Telegram ID.

### 4.3. Вспомогательные Классы и Enum

*   **`by.spvrent.service.enums.ServiceCommand`**:
    *   Enum, определяющий список поддерживаемых ботом команд (`/help`, `/registration`, `/cancel`, `/start`).
    *   Включает метод `fromValue` для преобразования строки в соответствующую команду.

*   **`by.spvrent.model.RabbitQueue` (предполагается, что это enum или константы)**:
    *   Определяет имена очередей RabbitMQ для обмена сообщениями.
    *   Используемые очереди: `TEXT_MESSAGE_UPDATE`, `DOC_MESSAGE_UPDATE`, `PHOTO_MESSAGE_UPDATE`, `ANSWER_MESSAGE`.

*   **`by.spvrent.exeption.UploadFileException`**:
    *   Пользовательское исключение для обработки ошибок, связанных с загрузкой файлов.

## 5. Конфигурация (application.properties/yml)

Этот микросервис требует следующих конфигурационных параметров:

*   **`bot.token`**: Токен вашего Telegram-бота, полученный от BotFather. Используется для доступа к Telegram Bot API.
*   **`service.file_info.uri`**: URL для получения метаданных файла из Telegram API. Пример: `https://api.telegram.org/bot{bot.token}/getFile?file_id={fileId}`.
*   **`service.file_storage.uri`**: URL для непосредственной загрузки файла из хранилища Telegram. Пример: `https://api.telegram.org/file/bot{bot.token}/{filePath}`.
*   **Конфигурация RabbitMQ**: (Не показано в коде, но подразумевается) Параметры подключения к RabbitMQ (хост, порт, учетные данные).
*   **Конфигурация Базы Данных**: (Не показано в коде, но подразумевается) Параметры подключения к вашей базе данных (URL, логин, пароль, драйвер).

## 6. Используемые Технологии и Библиотеки

*   **Spring Boot**: Фреймворк для быстрого создания готовых к продакшену Spring-приложений.
*   **Spring Data JPA**: Упрощает работу с базами данных с помощью ORM-маппинга (Hibernate).
*   **Lombok**: Сокращает boilerplate-код (геттеры, сеттеры, конструкторы и т.д.).
*   **TelegramBots**: Java-библиотека для взаимодействия с Telegram Bot API.
*   **Spring AMQP (RabbitMQ)**: Интеграция с RabbitMQ для обмена сообщениями.
*   **RestTemplate**: Для выполнения HTTP-запросов к внешним API (в данном случае к Telegram API).
*   **`org.json`**: Для парсинга JSON-ответов от Telegram API.

## 7. Дальнейшее развитие и TODO

В коде есть следующие области для доработки:

*   **Обработка Email (`MainServiceImpl:20`)**: `//TODO добавить обработку email`
*   **Регистрация пользователя (`MainServiceImpl:117`)**: `//TODO добавить регистрацию` (сейчас пользователь всегда `isActive = true`).
*   **Сохранение Фотографий (`MainServiceImpl:70`)**: `//TODO добавить сохранение фото;`

--- 