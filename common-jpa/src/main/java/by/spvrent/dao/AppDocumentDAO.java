package by.spvrent.dao;

import by.spvrent.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentDAO extends JpaRepository<AppDocument,Long> {
}
