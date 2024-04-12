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
                (t, l) -> "Valid, non-empty email is required");
        Translations.registerExceptionTranslator(InvalidPasswordException.class,
                (t, l) -> "Invalid password");
        Translations.registerExceptionTranslator(IncorrectPasswordException.class,
                (t, l) -> "Incorrect password");
    }
}
