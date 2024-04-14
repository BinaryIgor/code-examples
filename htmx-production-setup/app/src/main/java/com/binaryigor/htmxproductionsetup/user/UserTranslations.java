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
        Translations.registerExceptionTranslator(InvalidEmailException.class,
                (t, l) -> "Valid, non-empty email is required. It must contain '@' and a proper domain");
        Translations.registerExceptionTranslator(InvalidPasswordException.class,
                (t, l) -> "Invalid password. It must have at least 8 characters, one uppercase, one lowercase and one digit");
        Translations.registerExceptionTranslator(IncorrectPasswordException.class,
                (t, l) -> "Incorrect password");
    }
}
