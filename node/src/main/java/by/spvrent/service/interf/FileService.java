package by.spvrent.service.interf;

import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage);
    AppPhoto processPhoto(Message telegramMessage);
}
