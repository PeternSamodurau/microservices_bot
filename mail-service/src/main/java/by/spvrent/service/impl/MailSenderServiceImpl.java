// by.spvrent.service.impl.MailSenderServiceImpl.java
package by.spvrent.service.impl;

import by.spvrent.dto.MailParams;
import by.spvrent.service.interf.MailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${service.activation.uri}")
    private String activationServiceUri;

    @Override
    public void send(MailParams mailParams) {
        log.info("Отправка письма...");
        log.info("mailParams: {}", mailParams);
        log.info("emailFrom: {}", emailFrom);
        log.info("emailTo: {}", mailParams.getMailTo()); // Используем emailTo
        log.info("mailHost: {}", mailHost);
        log.info("mailPort: {}", mailPort);

        String subject = "Активация учетной записи";
        String messageBody = getActivationMailBody(mailParams.getId());
        String emailTo = mailParams.getMailTo(); // Используем emailTo

        if (emailTo == null || emailTo.isEmpty()) {
            log.error("Email to is null or empty!");
            return;
        }

        if (emailFrom == null || emailFrom.isEmpty()) {
            log.error("Email from is null or empty! Check your application.properties/yml");
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Важно: Укажите кодировку UTF-8
            helper.setFrom(emailFrom);
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setText(messageBody, false); // false indicates plain text

            javaMailSender.send(message);

            log.info("Письмо успешно отправлено на {}", emailTo);


        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма: {}", e.getMessage(), e); // Логируем объект исключения
        }
    }

    private String getActivationMailBody(String id) {
        String msg = String.format("Для завершения регистрации пройдите по ссылке: \n%s", activationServiceUri);
        return msg.replace("{id}", id);
    }
}