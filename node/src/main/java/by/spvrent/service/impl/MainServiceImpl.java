package by.spvrent.service.impl;

import by.spvrent.dao.AppUserDAO;
import by.spvrent.entity.*;
import by.spvrent.dao.RawDataDAO;
import by.spvrent.exeption.UploadFileException;
import by.spvrent.service.enums.LinkType;
import by.spvrent.service.enums.ServiceCommand;
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

    @Override
    @Transactional
    public void processTextMessage(Update update) {
        saveRawData(update);

        AppUser appUser = findOrSaveAppUser(update);
        AppUserState appUserState = appUser.getAppUserState();  // BASIC_STATE, WAIT_FOR_EMAIL_STATE
        String text = update.getMessage().getText();       // HELP("/help"), REGISTRATION("/registration"), CANCEL("/cancel"),START("/start");
        String output = "";

        ServiceCommand serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)){
            output = cancelProcess(appUser);
        }else if (BASIC_STATE.equals(appUserState)){
            output = processServiceComand(appUser,text);
        }else if (WAIT_FOR_EMAIL_STATE.equals(appUserState)){
            //TODO добавить обработку email
        }else {
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

        if (isNotAllowToSendContent(chatId,appUser)){
            return;
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

        if (isNotAllowToSendContent(chatID,appUser)){
            return;
        }
        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);

            var answer = "Фото успешно загружено! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatID);

        } catch (UploadFileException ex) {
            log.error("",ex);
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, chatID);
        }
    }

    private boolean isNotAllowToSendContent(Long chatID, AppUser appUser) {
        AppUserState appUserState = appUser.getAppUserState();
        if (!appUser.getIsActive()){
            String error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error,chatID);
            return true;
        }else if (!BASIC_STATE.equals(appUserState)){
            String error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error,chatID);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID);
        sendMessage.setText(output);

        log.info("Send message: " + sendMessage);

        producerService.producerAnswer(sendMessage);

    }

    private String processServiceComand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)){
            //TODO добавить регистрацию
            return "Временно не доступно!";
        } else if (HELP.equals(cmd)){
            return help();
        }else if (START.equals(cmd)){
            return "Привет! Чтобы посмотреть список доступных команд введите /help";
        }else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }

    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей комады;\n"
                + "/registration - регистрация пользователя;\n";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setAppUserState(BASIC_STATE);
        appUserDAO.save(appUser);

        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update){

        User telegramUser = update.getMessage().getFrom();

        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());

        if (persistentAppUser == null){
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .appUserState(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .update(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
