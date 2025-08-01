package by.spvrent.service.interf;

import by.spvrent.entity.AppBinaryContent;
import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import org.springframework.core.io.FileSystemResource;

public interface FileService {

    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);

}
