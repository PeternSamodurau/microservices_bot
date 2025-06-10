При запуске приложения на сервере

Spring сканирует компоненты с аннотациями @Configuration: 
Spring Application Context просматривает все классы в вашем проекте в поисках аннотаций @Configuration. 
Эти классы содержат конфигурацию для Spring компонентов.

1. BotConfigurationProperties:
  
- @Configuration: Обозначает, что это класс конфигурации Spring.
- @PropertySource("classpath:application.properties"): 
  Указывает, что значения полей этого класса нужно брать из файла application.properties, 
  расположенного в classpath (обычно это папка src/main/resources).
- @Data: Аннотация Lombok. Автоматически генерирует геттеры, сеттеры, toString(), equals() и hashCode() методы для всех полей класса. 
- @Value("${bot.name}"): Аннотация Spring. Внедряет значение свойства bot.name из файла application.properties в поле botName.
- @Value("${bot.token}"): Аннотация Spring. Внедряет значение свойства bot.token из файла application.properties в поле botToken.

Функция: Этот класс загружает настройки для вашего Telegram бота (имя и токен) из файла конфигурации 
         и делает их доступными для других компонентов Spring.

2. BotInitializer:
   
- @Configuration: Обозначает, что это класс конфигурации Spring.
- @Autowired private TelegramBotController bot;: Внедряет экземпляр TelegramBotController (нашего основного класса бота).
- @Autowired private TelegramBotsApi telegramBotsApi;: Внедряет экземпляр TelegramBotsApi (интерфейс для работы с Telegram Bots API).
- @PostConstruct public void registerBot() { ... }: @PostConstruct означает, 
  что этот метод будет вызван Spring после того, как все зависимости будут внедрены. 
  В этом методе происходит регистрация вашего бота в Telegram API.
- telegramBotsApi.registerBot(bot);: Вызывает метод registerBot() у TelegramBotsApi, передавая ему экземпляр TelegramBotController. 
  Это связывает ваш бот с Telegram API!!!!!!!
- try ... catch: Обрабатывает исключение TelegramApiException, которое может возникнуть, если регистрация не удалась, и записывает информацию об ошибке в лог.
- @Bean public OkHttpClient okHttpClient() { ... }: @Bean означает, что этот метод создает и настраивает бин типа OkHttpClient.
  OkHttpClient - это HTTP клиент из библиотеки OkHttp, который используется для отправки запросов к Telegram API (хотя это не видно в этом фрагменте кода, он, вероятно, используется внутри TelegramLongPollingBot).
  retryOnConnectionFailure(true) - Настройка, которая указывает OkHttpClient автоматически повторять запросы при возникновении проблем с соединением.
- @Bean public ObjectMapper objectMapper() { ... }: @Bean означает, что этот метод создает и настраивает бин типа ObjectMapper.
  ObjectMapper - это класс из библиотеки Jackson, который используется для преобразования Java объектов в JSON и обратно (сериализации и десериализации). Он, вероятно, используется для обмена данными с Telegram API.

Функция: Этот класс выполняет инициализацию, необходимую для работы Telegram бота: 
  регистрирует бота в Telegram API и создает необходимые компоненты (HTTP клиент, JSON mapper).

3. RabbitConfiguration (dispatcher):
   package by.spvrent.configuration;

- @Configuration: Обозначает, что это класс конфигурации Spring.
- @Bean public MessageConverter jsonMessageConverter(){ ... }: 
  @Bean означает, что этот метод создает и настраивает бин типа MessageConverter.
  MessageConverter - это компонент из Spring AMQP (Advanced Message Queuing Protocol), 
  который отвечает за преобразование Java объектов в сообщения, которые можно отправлять в RabbitMQ, и обратно.
  Jackson2JsonMessageConverter - Конкретная реализация MessageConverter, 
  которая использует библиотеку Jackson для преобразования объектов в JSON формат.

Функция: Этот класс создает бин, который позволяет Spring AMQP отправлять и получать сообщения в формате JSON через RabbitMQ.

4. RabbitConfiguration (node):
   package by.spvrent.configuration;
- Эта версия класса RabbitConfiguration расширяет функциональность предыдущей версии:
- import org.springframework.amqp.core.Queue;: Импортирует класс Queue из Spring AMQP.
- import static by.spvrent.model.RabbitQueue.*;: Импортирует статические константы из класса by.spvrent.model.RabbitQueue. 
  Эти константы, вероятно, представляют собой имена очередей RabbitMQ.
- @Bean public Queue textMessageQueue(){ ... }: Создает бин типа Queue с именем, указанным в константе TEXT_MESSAGE_UPDATE. 
  Это определяет очередь RabbitMQ для обработки текстовых сообщений от пользователей.
  Аналогичные методы docMessageQueue(), photoMessageQueue(), answerMessageQueue(): 
  Создают очереди RabbitMQ для обработки документов, фотографий и ответных сообщений, соответственно.

Функция: Этот класс настраивает взаимодействие с RabbitMQ, определяя очереди и бин для преобразования сообщений в JSON формат. 
         Он сообщает Spring AMQP, какие очереди нужно создать и использовать для обмена сообщениями между вашим ботом 
         и другими микросервисами.

5. TelegramBotsApiConfig:

- @Configuration: Обозначает, что это класс конфигурации Spring.
- @Bean public TelegramBotsApi telegramBotsApi() throws TelegramApiException { ... }: Создает и настраивает бин типа TelegramBotsApi.
  TelegramBotsApi - это интерфейс из Telegram Bots API, который используется для регистрации и управления ботами.
  DefaultBotSession.class - Указывает, что нужно использовать стандартную реализацию сессии для подключения к Telegram API (используя Long Polling).
- throws TelegramApiException: Указывает, что метод может выбросить исключение, если произошла ошибка при создании TelegramBotsApi.

Функция: Этот класс создает бин TelegramBotsApi, который необходим для регистрации и запуска Telegram бота.

В итоге:
Эти конфигурационные классы определяют основные компоненты и их настройки для работы Telegram бота и взаимодействия с RabbitMQ.
Они сообщают Spring, как создать и настроить эти компоненты, используя аннотации. 
