package by.spvrent.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "app_document")
@Entity
public class AppDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String telegramFileId;

    private String docName;

    @OneToOne
    private AppBinaryContent binaryContent;

    private String mimeType;

    private Long fileSize;

}
