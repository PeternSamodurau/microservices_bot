package by.spvrent.service.impl;

import by.spvrent.dao.AppUserDAO;
import by.spvrent.entity.AppUser;
import by.spvrent.service.interf.UserActivationService;
import by.spvrent.utils.CryptoTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDAO appUserDAO;
    private final CryptoTool cryptoTool;


    @Override
    public boolean activation(String cryptoUserId) {

        Long userId = cryptoTool.idOf(cryptoUserId);
        Optional<AppUser> optional = appUserDAO.findById(userId);

        if (optional.isEmpty()){

            AppUser user = optional.get();
            user.setIsActive(true);
            appUserDAO.save(user);

            return true;
        }
        return false;
    }
}
