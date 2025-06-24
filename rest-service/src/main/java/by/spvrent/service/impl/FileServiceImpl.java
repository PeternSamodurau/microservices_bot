package by.spvrent.service.impl;

import by.spvrent.dao.AppDocumentDAO;
import by.spvrent.dao.AppPhotoDAO;
import by.spvrent.entity.AppBinaryContent;
import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import by.spvrent.service.interf.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;


    @Override
    public AppDocument getDocument(String docId) {
        //TODO добавить дешифрование хэш строки
        Long longId = Long.parseLong(docId);
        return appDocumentDAO.findById(longId).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        //TODO добавить дешифрование хэш строки
        Long longId = Long.parseLong(photoId);
        return appPhotoDAO.findById(longId).orElse(null);

    }

    @Override
    public FileSystemResource getFileSystemResource(AppBinaryContent binaryContent) {
        try {
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp,binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }
}
