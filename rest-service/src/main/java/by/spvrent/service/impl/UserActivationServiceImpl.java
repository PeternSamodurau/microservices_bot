package by.spvrent.service.impl;

import by.spvrent.dao.AppUserDAO;
import by.spvrent.entity.AppUser;
import by.spvrent.service.interf.UserActivationService;
import by.spvrent.utils.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Добавьте эту аннотацию, если ее нет

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;

    @Override
    @Transactional // Важно для выполнения операций с БД внутри сервиса
    public boolean activation(String cryptoUserId) {
        log.info("Attempting user activation for crypto ID: {}", cryptoUserId);

        Long userId = cryptoTool.idOf(cryptoUserId);
        log.info("Decoded userId: {}", userId); // Добавим логирование для отладки

        Optional<AppUser> optionalUser = appUserDAO.findById(userId);

        // Если Optional НЕ пуст (т.е. пользователь найден)
        if (optionalUser.isPresent()){
            AppUser user = optionalUser.get();

            // Дополнительная проверка: убедитесь, что пользователь еще не активен
            if (user.getIsActive() != null && user.getIsActive()){
                log.warn("User with ID {} is already active.", userId);
                return false; // Пользователь уже активирован
            }

            user.setIsActive(true); // Активируем пользователя
            appUserDAO.save(user); // Сохраняем изменения

            log.info("User with ID {} successfully activated.", userId);
            return true; // Активация успешна
        } else {
            log.warn("User with decoded ID {} not found for activation.", userId);
            return false; // Пользователь не найден
        }
    }
}