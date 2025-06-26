// by.spvrent.controller.MailController.java
package by.spvrent.controller;

import by.spvrent.dto.MailParams;
import by.spvrent.service.interf.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final MailSenderService mailSenderService;

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        log.info("Received request: {}", mailParams);
        log.info("Received request to send activation email to: {}", mailParams.getMailTo());
        mailSenderService.send(mailParams);
        return ResponseEntity.ok().build();
    }
}