package by.spvrent.controller;

import by.spvrent.entity.AppBinaryContent;
import by.spvrent.entity.AppDocument;
import by.spvrent.entity.AppPhoto;
import by.spvrent.service.interf.FileService;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @RequestMapping(method = RequestMethod.GET, value = "/get-doc")
    public void getDoc(@RequestParam("id") String id, HttpServletResponse response) {

        AppDocument appDocument = fileService.getDocument(id);
        if (appDocument == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ;
        }
        response.setContentType(String.valueOf(MediaType.parseMediaType(appDocument.getMimeType())));
        response.setHeader("Content-disposition","attachment; filename=" + appDocument.getDocName());
        response.setStatus(HttpServletResponse.SC_OK);

        AppBinaryContent appBinaryContent = appDocument.getBinaryContent();

        try{
            ServletOutputStream out = response.getOutputStream();
            out.write(appBinaryContent.getFileAsArrayOfBytes());
            out.close();
        }catch (IOException e){
            log.error("",e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-photo")
    public void getPhoto(@RequestParam("id") String id, HttpServletResponse response) {

        AppPhoto appPhoto = fileService.getPhoto(id);
        if (appPhoto == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setContentType(MediaType.IMAGE_JPEG.toString());
        response.setHeader("Content-disposition","attachment;");
        response.setStatus(HttpServletResponse.SC_OK);

        AppBinaryContent appBinaryContent = appPhoto.getBinaryContent();

        try{
            ServletOutputStream out = response.getOutputStream();
            out.write(appBinaryContent.getFileAsArrayOfBytes());
            out.close();
        }catch (IOException e){
            log.error("",e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }
}

