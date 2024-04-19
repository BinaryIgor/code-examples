package com.binaryigor.htmxproductionsetup.user;

import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import com.binaryigor.htmxproductionsetup.user.domain.exception.IncorrectPasswordException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidEmailException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidPasswordException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class UserTranslations {

    @PostConstruct
    void registerTranslations() {
        Translations.registerExceptionTranslation(InvalidEmailException.class,
                (t, l) -> "Valid, non-empty email is required. It must contain '@' and a proper domain");
        Translations.registerExceptionTranslation(InvalidPasswordException.class,
                (t, l) -> "Invalid password. It must have at least 8 characters, one uppercase, one lowercase and a digit");
        Translations.registerExceptionTranslation(IncorrectPasswordException.class,
                (t, l) -> "Incorrect password");
    }
}
