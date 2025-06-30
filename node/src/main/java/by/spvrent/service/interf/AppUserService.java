package by.spvrent.service.interf;

import by.spvrent.entity.AppUser;

public interface AppUserService {

    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}
