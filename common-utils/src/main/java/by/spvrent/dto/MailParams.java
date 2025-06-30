package by.spvrent.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParams {

    private String id;

    private String mailTo;
}
