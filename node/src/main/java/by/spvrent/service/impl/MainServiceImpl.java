package by.spvrent.service.impl;

import by.spvrent.dao.AppUserDAO;
import by.spvrent.entity.*;
import by.spvrent.dao.RawDataDAO;
import by.spvrent.exeption.UploadFileException;
import by.spvrent.service.enums.LinkType;
import by.spvrent.service.enums.ServiceCommand;
import by.spvrent.service.interf.AppUserService;
import by.spvrent.service.interf.FileService;
import by.spvrent.service.interf.MainService;
import by.spvrent.service.interf.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static by.spvrent.entity.AppUserState.BASIC_STATE;
import static by.spvrent.entity.AppUserState.WAIT_FOR_EMAIL_STATE;
import static by.spvrent.service.enums.ServiceCommand.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FileService fileService;
    private final AppUserService appUserService;

    @Override
    @Transactional
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);
        AppUserState appUserState = appUser.getAppUserState();
        String text = update.getMessage().getText();
        String output = "";

        // --- Извлечение и очистка команды ---
        String parsedCommandText = null;
        if (text != null && text.startsWith("/")) {
            parsedCommandText = text.split(" ")[0].split("@")[0].trim();
        }
        ServiceCommand incomingServiceCommand = ServiceCommand.fromValue(parsedCommandText);

        // Добавлено для отладки состояния пользователя
        log.info("NODE - MainServiceImpl: User (ID: {}) state is '{}' for text '{}', parsedCommandText: '{}', incomingServiceCommand: {}",
                appUser.getTelegramUserId(), appUserState, text, parsedCommandText, incomingServiceCommand);

        // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ: Обработка команд, которые должны работать в любом состоянии, ПЕРЕД проверкой состояния ---
        if (CANCEL.equals(incomingServiceCommand)) { // /cancel всегда работает
            output = cancelProcess(appUser);
        } else if (HELP.equals(incomingServiceCommand)) { // /help всегда работает
            output = help();
        } else if (START.equals(incomingServiceCommand)) { // /start всегда работает
            output = "Привет! Чтобы посмотреть список доступных команд введите /help";
        }
        // --- Конец блока универсальных команд ---

        // Теперь обрабатываем в зависимости от состояния, ЕСЛИ команда не была универсальной
        else if (BASIC_STATE.equals(appUserState)) {
            // Если пользователь в BASIC_STATE, и это не универсальная команда
            // (т.е. incomingServiceCommand был null, или это не одна из универсальных команд,
            // но из fromValue вернулось что-то)
            output = processServiceCommand(appUser, incomingServiceCommand); // Передаем объект ServiceCommand (может быть null)
        } else if (WAIT_FOR_EMAIL_STATE.equals(appUserState)) {
            // Если пользователь в состоянии ожидания email, обрабатываем текст как email
            // (здесь text - это email, НЕ команда /help, потому что /help уже был обработан выше)
            output = appUserService.setEmail(appUser, text);
        } else {
            // Неизвестное состояние пользователя - это ошибка в логике приложения
            log.error("Unknown user state: " + appUserState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        Long chatID = update.getMessage().getChatId();
        sendAnswer(output, chatID);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();

        // Убедитесь, что isNotAllowToSendContent не меняет состояние, если оно не должно
        // и что он возвращает true, если загрузка контента не разрешена.
        // Если isNotAllowToSendContent сам устанавливает WAIT_FOR_EMAIL_STATE,
        // то это должно быть логикой регистрации.
        if (isNotAllowToSendContent(chatId, appUser)){
            return; // Выходим, если загрузка не разрешена
        }

        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);

            var answer = "Документ успешно загружен! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error("", ex);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);

        Long chatID = update.getMessage().getChatId();

        // Аналогично для фото
        if (isNotAllowToSendContent(chatID, appUser)){
            return;
        }

        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);

            var answer = "Фото успешно загружено! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatID);

        } catch (UploadFileException ex) {
            log.error("", ex);
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, chatID);
        }
    }

    private boolean isNotAllowToSendContent(Long chatID, AppUser appUser) {
        AppUserState appUserState = appUser.getAppUserState();
        if (!appUser.getIsActive()) {
            // Если пользователь не активен, сообщаем ему о необходимости регистрации/активации
            String error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error, chatID);
            // Если после этого должна начинаться регистрация, убедитесь, что AppUserService.registerUser
            // или другой метод устанавливает WAIT_FOR_EMAIL_STATE.
            // НЕ МЕНЯЕМ СОСТОЯНИЕ ЗДЕСЬ, ЕСЛИ ЭТО ДОЛЖНА ДЕЛАТЬ ЛОГИКА РЕГИСТРАЦИИ.
            return true;
        } else if (!BASIC_STATE.equals(appUserState)) {
            // Если пользователь активен, но не в BASIC_STATE (например, в WAIT_FOR_EMAIL_STATE),
            // то он должен сначала отменить текущее действие.
            String error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatID);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(output);

        log.info("NODE - MainServiceImpl: Sending message to chatId {}: '{}'", chatID, output);

        producerService.producerAnswer(sendMessage);
    }

    // --- ИСПРАВЛЕННЫЙ МЕТОД: processServiceCommand ---
    // Этот метод теперь принимает объект ServiceCommand
    private String processServiceCommand(AppUser appUser, ServiceCommand cmdEnum) {
        // Если cmdEnum равно null, это означает, что fromValue не нашел команду,
        // или что это был обычный текст, не начинающийся с '/'.
        // Этот метод вызывается только из BASIC_STATE, если команда не была универсальной.
        if (cmdEnum == null) {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }

        // Мы используем ServiceCommand.ENUM_NAME.equals(cmdEnum), что является корректным сравнением объектов ENUM.
        if (REGISTRATION.equals(cmdEnum)) {
            // Этот метод (appUserService.registerUser) должен устанавливать
            // AppUserState.WAIT_FOR_EMAIL_STATE, если регистрация начинается/продолжается.
            return appUserService.registerUser(appUser);
        } else {
            // Если сюда попадает, значит, это известная команда, которая не является универсальной,
            // но и не является REGISTRATION (в рамках BASIC_STATE)
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }
    // --- КОНЕЦ ИСПРАВЛЕННОГО МЕТОДА ---

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей комады;\n"
                + "/registration - регистрация пользователя;\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setAppUserState(BASIC_STATE); // Сбрасываем состояние на BASIC_STATE
        appUserDAO.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Optional<AppUser> persistentAppUser = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (persistentAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .appUserState(BASIC_STATE) // Изначально всегда BASIC_STATE для нового пользователя
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }
}