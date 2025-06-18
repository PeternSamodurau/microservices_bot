package by.spvrent.service.impl;

import by.spvrent.dao.AppBinaryContentDAO;
import by.spvrent.dao.AppDocumentDAO;
import by.spvrent.entity.AppBinaryContent;
import by.spvrent.entity.AppDocument;
import by.spvrent.exeption.UploadFileException;
import by.spvrent.service.interf.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    @Value("${bot.token}") // ИСПРАВЛЕНО
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final AppDocumentDAO appDocumentDAO;
    private final AppBinaryContentDAO binaryContentDAO;

    @Override
    public AppDocument processDoc(Message telegramMessage) {

        Document telegramDoc = telegramMessage.getDocument();

        if (telegramDoc == null) {
            log.warn("Received message did not contain a document. Skipping processing.");
            throw new UploadFileException("Message does not contain a document.");
        }

        String fileId = telegramDoc.getFileId();
        ResponseEntity<String> fileInfoResponse;

        try {
            fileInfoResponse = getFileInfoFromTelegramApi(fileId);
        } catch (HttpClientErrorException e) {
            // ИСПРАВЛЕНО: используем e.getResponseBodyAsString()
            log.error("HTTP client error fetching file info for fileId: {}. Status: {}, Body: {}",
                    fileId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new UploadFileException("Failed to get file info from Telegram API due to client error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("General error fetching file info for fileId: {}", fileId, e);
            throw new UploadFileException("Failed to get file info from Telegram API: " + e.getMessage(), e);
        }

        if (fileInfoResponse.getStatusCode() == HttpStatus.OK) {
            String responseBody = fileInfoResponse.getBody();

            log.info("Telegram API Response Status for getFile: {}", fileInfoResponse.getStatusCode());
            log.info("Telegram API Response Body for getFile: {}", responseBody);

            if (responseBody == null || responseBody.trim().isEmpty() || !responseBody.trim().startsWith("{")) {
                log.error("Telegram API response for fileId {} is not a valid JSON object. Body: {}", fileId, responseBody);
                throw new UploadFileException("Telegram API response is not a valid JSON object. Check bot token or API endpoint format. Raw body: " + responseBody);
            }

            try {
                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.getBoolean("ok")) {
                    String filePath = jsonObject
                            .getJSONObject("result")
                            .getString("file_path");

                    log.info("Successfully retrieved file path from Telegram API: {}", filePath);

                    AppBinaryContent persistentBinaryContent = getPersistentBinaryContent(filePath);
                    AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
                    return appDocumentDAO.save(transientAppDoc);
                } else {
                    String errorDescription = jsonObject.optString("description", "No description provided.");
                    log.error("Telegram API returned 'ok:false' for fileId: {}. Description: {}", fileId, errorDescription);
                    throw new UploadFileException("Telegram API error: " + errorDescription);
                }
            } catch (org.json.JSONException e) {
                log.error("Failed to parse JSON response from Telegram API for fileId: {}. Raw body: {}", fileId, responseBody, e);
                throw new UploadFileException("Invalid JSON structure in Telegram API response.", e);
            } catch (Exception e) {
                log.error("An unexpected error occurred during document processing for fileId: {}", fileId, e);
                throw new UploadFileException("An unexpected error occurred while processing the document.", e);
            }
        } else {
            log.error("Bad response from Telegram API for fileId: {}. Status: {}, Body: {}",
                    fileId, fileInfoResponse.getStatusCode(), fileInfoResponse.getBody());
            throw new UploadFileException("Bad HTTP status from Telegram API: Status=" + fileInfoResponse.getStatusCode() + ", Body=" + fileInfoResponse.getBody());
        }
    }

    private AppBinaryContent getPersistentBinaryContent(String filePath) {
        byte [] fileInByte = downloadFile(filePath);
        AppBinaryContent transientBinaryContent = AppBinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
        return binaryContentDAO.save(transientBinaryContent);
    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, AppBinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private ResponseEntity<String> getFileInfoFromTelegramApi(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> request = new HttpEntity<>(headers);

        String url = fileInfoUri.replace("{bot.token}", token).replace("{fileId}", fileId);
        log.debug("Calling Telegram API to get file info: {}", url);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class
        );
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{bot.token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            log.error("Error creating URL for file download: {}. Provided file path: {}", fullUri, filePath, e);
            throw new UploadFileException("Invalid URL for file download: " + fullUri, e);
        }

        log.info("Attempting to download file from Telegram storage. URL: {}", fullUri);

        try (InputStream is = urlObj.openStream()) {
            byte[] fileBytes = is.readAllBytes();
            log.info("Successfully downloaded {} bytes from: {}", fileBytes.length, fullUri);
            return fileBytes;
        } catch (IOException e) {
            log.error("Failed to download file from URL: {}", fullUri, e);
            throw new UploadFileException("Failed to download file from URL: " + fullUri, e);
        }
    }
}