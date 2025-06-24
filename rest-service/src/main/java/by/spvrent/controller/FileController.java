package by.spvrent.controller;

import by.spvrent.entity.AppBinaryContent;
import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import by.spvrent.service.interf.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @RequestMapping(method = RequestMethod.GET, value = "/get-doc")
    public ResponseEntity<?> getDoc(@RequestParam("id") String id) {

        AppDocument appDocument = fileService.getDocument(id);
        if (appDocument == null){
            return ResponseEntity.badRequest().build();
        }
        AppBinaryContent appBinaryContent = appDocument.getBinaryContent();

        FileSystemResource fileSystemResource = fileService.getFileSystemResource(appBinaryContent);

        if (fileSystemResource == null){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(appDocument.getMimeType()))
                .header("Content-disposition", "attachment;")
                .body(fileSystemResource);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-photo")
    public ResponseEntity<?> getPhoto(@RequestParam("id") String id) {

        AppPhoto appPhoto = fileService.getPhoto(id);
        if (appPhoto == null){
            return ResponseEntity.badRequest().build();
        }
        AppBinaryContent appBinaryContent = appPhoto.getBinaryContent();

        FileSystemResource fileSystemResource = fileService.getFileSystemResource(appBinaryContent);

        if (fileSystemResource == null){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                .header("Content-disposition", "attachment;")
                .body(fileSystemResource);
    }
}

