package by.spvrent.service.interf;

import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import by.spvrent.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
