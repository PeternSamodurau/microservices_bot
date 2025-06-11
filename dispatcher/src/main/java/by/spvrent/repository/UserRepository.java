package by.spvrent.repository; // или by.spvrent.dao

import by.spvrent.entity.User; // Импортируйте вашу сущность
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Здесь можно добавить кастомные методы поиска
}