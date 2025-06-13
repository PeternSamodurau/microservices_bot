package by.spvrent.service.interf;

import by.spvrent.entity.AppDocument;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
