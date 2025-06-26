package by.spvrent.service.interf;

import by.spvrent.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
