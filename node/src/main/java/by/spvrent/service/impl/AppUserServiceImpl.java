package by.spvrent.service.impl;

import by.spvrent.dao.AppUserDAO;
import by.spvrent.dto.MailParams;
import by.spvrent.entity.AppUser;
import by.spvrent.entity.AppUserState;
import by.spvrent.service.interf.AppUserService;
import by.spvrent.utils.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    @Value("${service.mail.uri}")
    private String mailServiceUri;

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "Вы уже активировали свою учетную запись!";
        } else if (appUser.getEmail() != null) {
            // Это может произойти, если пользователь начал регистрацию, но письмо не дошло/он не активировал.
            // Можно предложить отправить письмо повторно, но пока оставим так.
            return "Вам уже было отправлено письмо. Пожалуйста, проверьте почту и перейдите по ссылке для активации.";
        }
        // Если не активен и нет email, начинаем процесс регистрации
        appUser.setAppUserState(AppUserState.WAIT_FOR_EMAIL_STATE);
        appUserDAO.save(appUser); // Сохраняем изменение состояния
        return "Для продолжения регистрации, пожалуйста, введите ваш email: ";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        log.info("NODE - AppUserServiceImpl: Attempting to set email: '{}' for user ID: {}", email, appUser.getTelegramUserId());

        // 1. Валидация email
        String trimmedEmail = (email != null) ? email.trim() : null;
        if (trimmedEmail == null || trimmedEmail.isEmpty()) {
            log.warn("NODE - AppUserServiceImpl: Email is null or empty after trim. Returning error.");
            return "Вы ввели пустую строку. Введите, пожалуйста, корректный email. Для отмены введите /cancel";
        }

        try {
            InternetAddress emailAddress = new InternetAddress(trimmedEmail);
            emailAddress.validate();
            log.info("NODE - AppUserServiceImpl: Email '{}' successfully passed validation.", trimmedEmail);
        } catch (AddressException e) {
            log.warn("NODE - AppUserServiceImpl: Email '{}' is invalid: {}", trimmedEmail, e.getMessage());
            return "Вы ввели некорректный email. Пожалуйста, попробуйте еще раз. Для отмены введите /cancel";
        }

        // 2. Проверка, не занят ли email
        Optional<AppUser> optional = appUserDAO.findByEmail(trimmedEmail);
        if (optional.isPresent()) {
            log.warn("NODE - AppUserServiceImpl: Email '{}' is already in use by another user.", trimmedEmail);
            return "Этот email уже используется. Введите другой email. Для отмены введите /cancel";
        }

        // --- ГЛАВНОЕ ИЗМЕНЕНИЕ: СНАЧАЛА ОТПРАВКА, ПОТОМ ИЗМЕНЕНИЕ СОСТОЯНИЯ ---

        // 3. Попытка отправить письмо
        try {
            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<String> response = sendRequestToMailService(cryptoUserId, trimmedEmail);

            // 4. Анализ ответа от сервиса почты
            if (response.getStatusCode() == HttpStatus.OK) {
                // УСПЕХ! Только теперь мы сохраняем email и меняем состояние пользователя
                appUser.setEmail(trimmedEmail);
                appUser.setAppUserState(AppUserState.BASIC_STATE);
                appUserDAO.save(appUser);

                log.info("NODE - AppUserServiceImpl: Successfully sent email and updated user state for email '{}'", trimmedEmail);
                return "Вам на почту было отправлено письмо. Перейдите по ссылке в письме для подтверждения регистрации.";
            } else {
                // Сервис почты вернул ошибку (например, 500)
                String errorMessage = String.format("Не удалось отправить письмо на почту %s (сервис ответил с кодом %s). Пожалуйста, попробуйте еще раз или введите /cancel.", trimmedEmail, response.getStatusCode());
                log.error(errorMessage);
                return errorMessage;
            }
        } catch (Exception e) {
            // Ловим сетевые ошибки (например, сервис недоступен, неверный URL)
            String errorMessage = String.format("Критическая ошибка при отправке письма на почту %s. Сервис отправки может быть недоступен. Пожалуйста, попробуйте позже или введите /cancel.", trimmedEmail);
            log.error(errorMessage, e); // Логируем полное исключение для отладки
            return errorMessage;
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        MailParams mailParams = MailParams.builder()
                .id(cryptoUserId)
                .mailTo(email)
                .build();

        HttpEntity<MailParams> request = new HttpEntity<>(mailParams, httpHeaders);

        log.info("NODE - AppUserServiceImpl: Sending request to mail service at '{}' for email '{}' with ID '{}'",
                mailServiceUri, email, cryptoUserId);

        return restTemplate.exchange(mailServiceUri, HttpMethod.POST, request, String.class);
    }
}