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
import java.net.InterfaceAddress;
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
            return "You are already activated!";
        } else if (appUser.getEmail() != null) {
            return "An email has been sent to you. Please follow the link in the email to confirm your registration.";
        }
        appUser.setAppUserState(AppUserState.WAIT_FOR_EMAIL_STATE);
        return "Enter your email: ";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {

        try {

            InternetAddress emailAddress = new InternetAddress();
            emailAddress.validate();

        } catch (Exception e) {
            return "Введите, пожалуйста, корректный email. Для отмены команды введите /cancel";
        }

        Optional<AppUser> optional = appUserDAO.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setAppUserState(AppUserState.BASIC_STATE);
            appUser = appUserDAO.save(appUser);

            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<String> response = sendRequestToMailService(cryptoUserId, email);

            if (response.getStatusCode() != HttpStatus.OK) {
                String message = String.format("Отправка письма на почту %s не удалась. ", email);
                log.error(message);

                appUser.setEmail(null);
                appUserDAO.save(appUser);

                return message;
            }
            return "Вам на почту было отправлено письмо. Перейдите по ссылке в письме для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите корректный email. Для отмены команды введите /cancel";
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

        HttpEntity<?> request = new HttpEntity<>(mailParams,httpHeaders);

        return restTemplate.exchange(mailServiceUri, HttpMethod.POST, request, String.class);
    }
}
